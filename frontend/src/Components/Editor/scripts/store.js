import { writable } from 'svelte/store';

export const projectNameStore = writable("");
export const errorStore = writable("");
export const textareaStore = writable("");
export const editorWrapperHeightStore = writable(0);
export const currentHighlightedItemStore = writable(null);

export const showCreateFileStore = writable(null);
export const showCreateFolderStore = writable(null);
export const currentContextMenuStore = writable(null);
export const showRenameStore = writable(null);

export const currentFileStore = writable(null);
export const deleteFileStore = writable(null);
export const renameFileStore = writable(null);
export const createFileStore = writable(null);
export const moveToItemIdStore = writable(null);
export const currentFileNameStore = writable(null);

export const currentFolderStore = writable(null);
export const deleteFolderStore = writable(null);
export const deleteFolderFileIdStore = writable(null);
export const deleteFolderFolderIdStore = writable(null);
export const renameFolderStore = writable(null);
export const createFolderStore = writable(null);
export const currentlyOpenedFolderStore = writable([]);

export const currentlyDraggedItemId = writable(null);
export const itemIsDraggedOver_ParentIdStore = writable(null);
export const forbbidenDragIdArrayStore = writable([]); 
export const currentlyDraggedItemNameStore = writable(null);

export const textareaValueStore = writable("");
export const openedRemoteFilesStore = writable(null);
export const connectedUsersStore = writable([]);

export const stompClientStore = writable(null);
export const activeProjectFilesStores = writable([]);
export const projectExplorer = writable(true);
export const projectAssistant = writable(false);
export const nameStore = writable("");
export const overseerStore = writable(false);
export const stopClientConnectStore = writable(false);

export const isViewerOrTemplateStore = writable(false);
