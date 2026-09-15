const fetch = require('node-fetch')
const { registry } = require('./env')

const tagReg = /^[a-z0-9]+$/
const tags = ['latest', 'alpha']

function approximateCode (a, b, strict) {
  if (strict) {
    return Number(a) === Number(b)
  }
  // < 9
  return Math.min(Number(a), 9) === Math.min(Number(b), 9)
}

function approximateDate (a, b) {
  function toTime (str) {
    return new Date(`${str.substring(0, 4)}-${str.substring(4, 6)}-${str.substring(6)}`).getTime()
  }
  // <= 2d
  const diff = (toTime(a) - toTime(b)) / 86400000
  return Math.abs(diff) <= 2 ? (2 - Math.abs(diff) + (diff > 0 ? 0.1 : 0)) * 10000 : 0
}

async function find (package, target, vue3) {
  const versionReg = /\d+\.\d+\.\d+(-alpha)?-(\d+)/
  if (versionReg.test(target) && target[0] === (vue3 ? '3' : '2')) {
    return target
  }
  let nextVersion
  if (tags.includes(target)) {
    target = target === tags[0] ? 'release' : target
    const response = await fetch(`https://download1.dcloud.net.cn/hbuilderx/${target}.json`)
    const text = await response.text()
    const data = JSON.parse(text)
    target = data.version
  } else if (tagReg.test(target)) {
    const tag = target
    const response = await fetch(`${registry}/-/package/${package}/dist-tags`)
    const text = await response.text()
    const data = JSON.parse(text)
    nextVersion = data[tag]
    if (!nextVersion) {
      throw new Error('Invalid tag: ' + target)
    }
    return nextVersion
  }
  const result = target.match(/^(\d+)\.(\d+)\.(\d+)(?:\.(\d+))?(-alpha)?$/)
  if (!result) {
    throw new Error('Invalid version: ' + target)
  }
  let [_, tv1, tv2, tv3, tv4, tAlpha] = result
  // fix HBuilderX odd version
  if (tv3.length === 10) {
    tv4 = tv3.substring(0, 8)
    tv3 = tv2.length === 1 ? '0' : tv2[1]
    tv2 = tv2[0]
  }
  // Vue2 && <3.6
  if (!tv4 && Number(tv3) >= 9 && !vue3 && (Number(tv1) < 3 || (tv1 === '3' && Number(tv2) < 6))) {
    throw new Error('Need full version: ' + target.replace(/^(\d+\.\d+\.\d+)/, '$1.????????'))
  }
  const response = await fetch(`${registry}/${package}`, {
    headers: { 'Accept': 'application/vnd.npm.install-v1+json' }
  })
  const text = await response.text()
  const data = JSON.parse(text)
  const versions = Object.keys(data.versions)
  let nextVersionFix = 0
  function compare (version, oAlpha, ov1, ov2, ov3, ov4, ov5, strict) {
    const dataDiff = approximateDate(tv4 || ov4, ov4)
    const fix = dataDiff + Number(ov5)
    if (approximateCode(tv1, ov1) && approximateCode(tv2, ov2, strict) && approximateCode(tv3, ov3, strict) && dataDiff && fix > nextVersionFix) {
      if (vue3 || tAlpha === oAlpha) {
        nextVersion = version
        nextVersionFix = fix
      }
    }
  }
  for (let i = 0; i < versions.length; i++) {
    const version = versions[i]
    const result = version.match(versionReg)
    if (result) {
      const [_, oAlpha, oString] = result
      switch (oString.length) {
        case 16: {
          const [_, ov1, ov2, ov3, ov4, ov5] = oString.match(/(\d)(\d{2})(\d{2})(\d{8})(\d{3})/)
          compare(version, oAlpha, ov1, ov2, ov3, ov4, ov5, true)
          break
        }
        case 14: {
          const [_, ov1, ov2, ov3, ov4, ov5] = oString.match(/(\d)(\d)(\d)(\d{8})(\d{3})/)
          compare(version, oAlpha, ov1, ov2, ov3, ov4, ov5)
          break
        }
        default:
          break
      }
    }
  }
  if (!nextVersion) {
    throw new Error('Not find version: ' + target)
  }
  // console.log(`version: ${nextVersion}`)
  return nextVersion
}

module.exports = {
  find
}
