const spawn = require('cross-spawn')
const path = require('path')

const plugins = {
  vue2: '@dcloudio/vue-cli-plugin-uni',
  vue3: '@dcloudio/vite-plugin-uni'
}

function info (projectPath) {
  try {
    const data = require(path.join(projectPath, 'package.json'))
    const devDeps = data.devDependencies
    let vue
    let plugin
    if (plugins.vue3 in devDeps) {
      vue = 3
      plugin = plugins.vue3
    } else if (plugins.vue2 in devDeps) {
      vue = 2
      plugin = plugins.vue2
    } else {
      throw new Error('Invalid project: ' + projectPath)
    }
    return {
      plugin,
      vue
    }
  } catch (error) {
    throw new Error('Invalid project: ' + projectPath)
  }
}

function parseDeps (dependencies) {
  return Object.keys(dependencies).map(key => `${key}@${dependencies[key]}`)
}

const managers = [
  'npm',
  'yarn',
  'pnpm',
  'cnpm',
  'manual'
]

async function merge (projectPath, packageInfo, manager) {
  const args = []
  switch (manager) {
    case 'manual':
      console.log(packageInfo)
      return
    case 'yarn':
      args.push('add')
      break
    default:
      args.push('install')
      break
  }
  if (['npm', 'cnpm'].includes(manager)) {
    args.push('-E')
  }
  async function spawnAsync (manager, args) {
    const child = spawn(manager, args, {
      stdio: 'inherit',
      cwd: projectPath,
    })
    return new Promise((resolve, reject) => {
      child.on('error', reject)
      child.on('exit', resolve)
    })
  }
  await spawnAsync(manager, [...args, ...parseDeps(packageInfo.dependencies)])
  await spawnAsync(manager, [...args, ...parseDeps(packageInfo.devDependencies), '-D'])
}

module.exports = {
  info,
  managers,
  merge
}
