import { writable } from 'svelte/store';

export const errorStore = writable("");
export const textareaStore = writable("");
export const editorWrapperHeightStore = writable(0);
export const projectExplorer = writable(true);
export const projectAssistant = writable(false);
export const nameStore = writable("");
