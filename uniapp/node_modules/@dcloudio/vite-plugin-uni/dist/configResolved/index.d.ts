import { Plugin, ResolvedConfig } from 'vite';
import { VitePluginUniResolvedOptions } from '..';
export declare function createConfigResolved(options: VitePluginUniResolvedOptions): Plugin['configResolved'];
export declare function initLogger({ logger, nvue, }: ResolvedConfig & {
    nvue?: boolean;
}): void;
