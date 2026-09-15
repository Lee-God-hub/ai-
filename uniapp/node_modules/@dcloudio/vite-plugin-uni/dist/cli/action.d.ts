import type { BuildOptions, ServerOptions } from 'vite';
import { CliOptions } from '.';
export declare function runDev(options: CliOptions & ServerOptions): Promise<undefined>;
export declare function runBuild(options: CliOptions & BuildOptions): Promise<void>;
