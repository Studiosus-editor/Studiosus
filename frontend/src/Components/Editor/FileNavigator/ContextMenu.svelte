<script>
  import { createEventDispatcher } from "svelte";
  import { _ } from "svelte-i18n";

  export let x;
  export let y;
  export let visible;
  export let close;

  const dispatch = createEventDispatcher();

  function handleRename(event) {
    event.stopPropagation();
    dispatch("rename");
    close();
  }

  function handleDelete(event) {
    event.stopPropagation();
    dispatch("delete");
    close();
  }
</script>

{#if visible}
  <div
    class="context-menu"
    style="position: fixed; top: {y}px; left: {x}px;"
    on:click|stopPropagation
    on:keydown|stopPropagation
  >
    <button on:click={handleRename}><h4>{$_("contextMenu.rename")}</h4></button>
    <button on:click={handleDelete}><h4>{$_("contextMenu.delete")}</h4></button>
  </div>
{/if}

<style>
  .context-menu {
    background: white;
    border: 1px solid var(--white-smoke);
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    z-index: 100;
    cursor: default;
  }

  .context-menu button {
    display: block;
    padding: 2px 5px;
    z-index: 100;
    background: none;
    border: none;
    width: 100%;
    text-align: left;
    cursor: pointer;
  }
  button:hover {
    background-color: #d9e1ff;
  }

  h4 {
    margin: 0;
  }
</style>
