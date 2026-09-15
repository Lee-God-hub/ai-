"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.buildUVue = exports.runUVueBuild = exports.runUVueDev = exports.initUVueEnv = void 0;
const vite_1 = require("vite");
const shared_1 = require("@vue/shared");
const uni_cli_shared_1 = require("@dcloudio/uni-cli-shared");
const build_1 = require("./build");
const utils_1 = require("./utils");
const easycom_1 = require("../utils/easycom");
function initUVueEnv() {
    // 直接指定了
    if (process.env.UNI_APP_X === 'false') {
        return;
    }
    const manifestJson = (0, uni_cli_shared_1.parseManifestJsonOnce)(process.env.UNI_INPUT_DIR);
    const isNVueEnabled = (0, shared_1.hasOwn)(manifestJson, 'uni-app-x');
    if (!isNVueEnabled) {
        return;
    }
    process.env.UNI_APP_X = 'true';
}
exports.initUVueEnv = initUVueEnv;
async function runUVueDev(options) {
    if (options.platform !== 'app') {
        (0, uni_cli_shared_1.output)('error', uni_cli_shared_1.M['uvue.unsupported'].replace('{platform}', options.platform));
        return process.exit(0);
    }
    (0, easycom_1.initEasycom)();
    const watcher = (await buildUVue(options));
    let isFirstStart = true;
    let isFirstEnd = true;
    watcher.on('event', (event) => {
        if (event.code === 'BUNDLE_START') {
            if (isFirstStart) {
                isFirstStart = false;
                return;
            }
            (0, uni_cli_shared_1.output)('log', uni_cli_shared_1.M['dev.watching.start']);
        }
        else if (event.code === 'BUNDLE_END') {
            event.result.close();
            if (isFirstEnd) {
                // 首次全量同步
                isFirstEnd = false;
                (0, uni_cli_shared_1.output)('log', uni_cli_shared_1.M['dev.watching.end']);
                (0, utils_1.printStartupDuration)((0, vite_1.createLogger)(options.logLevel), false);
                return;
            }
            const dex = process.env.UNI_APP_UTS_CHANGED_FILES;
            process.env.UNI_APP_UTS_CHANGED_FILES = '';
            if (dex) {
                return (0, uni_cli_shared_1.output)('log', uni_cli_shared_1.M['dev.watching.end.files'].replace('{files}', JSON.stringify(JSON.parse(dex))));
            }
            return (0, uni_cli_shared_1.output)('log', uni_cli_shared_1.M['dev.watching.end']);
        }
    });
}
exports.runUVueDev = runUVueDev;
async function runUVueBuild(options) {
    try {
        await buildUVue(options);
        console.log(uni_cli_shared_1.M['build.done']);
    }
    catch (e) {
        console.error(`Build failed with errors.`);
        process.exit(1);
    }
}
exports.runUVueBuild = runUVueBuild;
/**
 * 目前的简易实现逻辑
 * node层：
 *  1. 监听项目，生成资源到临时目录 .uts/android, .uts/ios
 *  2. uvue 文件，做解析，拆分生成 render.kt, css.kt, uts.uvue
 *  3. static 文件，copy 到最终目录
 *  4. uvue、vue、uts 文件发生变化，调用 uts 编译器
 * @param options
 */
async function buildUVue(options) {
    return (0, build_1.buildByVite)((0, utils_1.addConfigFile)((0, shared_1.extend)({ nvueAppService: true, nvue: true }, (0, build_1.initBuildOptions)(options, (0, utils_1.cleanOptions)(options)))));
}
exports.buildUVue = buildUVue;
