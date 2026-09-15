const fetch = require('node-fetch')
const { repository, registry, registries } = require('./env')

async function getVue3 (version, origin = version, tag) {
  const target = tag || version
  let response = await fetch(`${repository}/${target}/package.json`)
  const text = await response.text()
  let data
  try {
    data = JSON.parse(text)
  } catch (error) {
    const array = version.split('')
    const n = Number(array[array.length - 1])
    if (n > 1) {
      array[array.length - 1] = n - 1
      return await getVue3(array.join(''), origin)
    }
    if (!tag) {
      tag = version.includes('alpha') ? 'vite-alpha' : 'vite'
      return await getVue3(origin, origin, tag)
    }
    throw new Error('Invalid version: ' + version)
  }
  const pluginVersion = data.devDependencies['@dcloudio/vite-plugin-uni']
  function parse (key) {
    const source = data[key]
    const object = {}
    for (const key in source) {
      const value = source[key]
      object[key] = value === pluginVersion ? version : value
    }
    return object
  }
  const package = {
    dependencies: parse('dependencies'),
    devDependencies: parse('devDependencies')
  }
  return package
}

async function get (version, vue3) {
  if (vue3) {
    return await getVue3(version)
  }
  const url = registry === registries.cnpm ? `https://registry.npmmirror.com/@dcloudio/vue-cli-plugin-uni/${version}/files/generator.js` : `https://unpkg.com/@dcloudio/vue-cli-plugin-uni@${version}/generator.js`
  const response = await fetch(url)
  const text = await response.text()
  function parse (prop) {
    const reg = new RegExp(`${prop}:\\s*\\{(.+?)\\}`, 's')
    const array = text.match(reg)[1].split(',')
    const object = {}
    array.forEach(item => {
      let [key, value] = item.split(':').map(item => item.trim())
      object[key.replace(/'/g, '')] = value === 'version' ? version : value.replace(/'/g, '')
    })
    return object
  }
  const package = {
    dependencies: parse('dependencies'),
    devDependencies: parse('devDependencies')
  }
  return package
}

module.exports = {
  get
}
