#!/usr/bin/env node
const minimist = require('minimist')
const inquirer = require('inquirer')
const { find } = require('../lib/version')
const { get } = require('../lib/package')
const { managers, merge, info } = require('../lib/project')

const argv = minimist(process.argv.slice(2))
const target = argv._[0] || 'latest'
const project = argv.project || process.cwd()
let manager = argv.manager

async function start() {
  const { plugin, vue } = info(project)
  const vue3 = vue === 3
  const version = await find(plugin, target, vue3)
  const deps = await get(version, vue3)
  if (!managers.includes(manager)) {
    const answers = await inquirer.prompt([{
      type: 'list',
      name: 'manager',
      message: 'Select Package Manager',
      choices: managers
    }])
    manager = answers.manager
  }
  await merge(project, deps, manager)
}

start()
