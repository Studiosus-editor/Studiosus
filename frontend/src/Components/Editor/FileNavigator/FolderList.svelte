<script>
  // Helper component to render a list of folders without calling Folder component recursively inside itself
  import Folder from "./Folder.svelte";

  export let childFolders;
</script>

{#each childFolders as childFolder}
  <Folder
    folder={childFolder}
    isNested={true}
    folderName={childFolder.name}
    id={childFolder.id}
    parentFolderId={childFolder.parentFolderId}
  />
{/each}
