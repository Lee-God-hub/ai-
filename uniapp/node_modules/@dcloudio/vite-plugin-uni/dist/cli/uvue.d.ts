import { BuildOptions, ServerOptions } from 'vite';
import { RollupWatcher } from 'rollup';
import { CliOptions } from '.';
export declare function initUVueEnv(): void;
export declare function runUVueDev(options: CliOptions & ServerOptions): Promise<undefined>;
export declare function runUVueBuild(options: CliOptions & BuildOptions): Promise<void>;
/**
 * 目前的简易实现逻辑
 * node层：
 *  1. 监听项目，生成资源到临时目录 .uts/android, .uts/ios
 *  2. uvue 文件，做解析，拆分生成 render.kt, css.kt, uts.uvue
 *  3. static 文件，copy 到最终目录
 *  4. uvue、vue、uts 文件发生变化，调用 uts 编译器
 * @param options
 */
export declare function buildUVue(options: CliOptions): Promise<RollupWatcher | void>;
