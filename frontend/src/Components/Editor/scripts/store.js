import { writable } from 'svelte/store';

export const errorStore = writable("");
export const textAreaStore = writable("");
export const editorWrapperHeightStore = writable(0);
export const projectExpanded = writable(true);