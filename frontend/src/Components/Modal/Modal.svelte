<script>
  import { onMount, onDestroy, tick } from "svelte";
  import exitIcon from "../../assets/svg/exit-cross-black.svg";
  import { createEventDispatcher } from "svelte";
  import { _ } from "svelte-i18n";

  export let panelName;
  export let width;

  const dispatch = createEventDispatcher();

  let isOpen = false;
  let content;

  const disableScroll = () => {
    document.body.style.overflow = "hidden";
  };

  const enableScroll = () => {
    document.body.style.overflow = "auto";
  };

  const closeModal = () => {
    isOpen = false;
    enableScroll();
    dispatch("closeModal");
  };

  const handleBackdropClick = (event) => {
    if (event.target.classList.contains("backdrop")) {
      closeModal();
    }
  };

  onMount(async () => {
    isOpen = true;
    disableScroll();
    await tick();
    if (content) content.focus();
  });

  onDestroy(() => {
    enableScroll();
  });
</script>

{#if isOpen}
  <div
    class="backdrop"
    on:click={handleBackdropClick}
    on:keydown={handleBackdropClick}
  >
    <div
      bind:this={content}
      class="backdrop__modal-content"
      on:click|stopPropagation
      on:keydown|stopPropagation
      style="width: {width};"
      tabindex="-1"
    >
      <h1 class="backdrop__header">{panelName}</h1>
      <img
        class="backdrop__exit-icon"
        src={exitIcon}
        alt={$_("modal.close")}
        on:click={closeModal}
        on:keydown={closeModal}
      />
      <div class="backdrop__line"></div>
      <slot />
    </div>
  </div>
{/if}

<style lang="scss">
  .backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(2px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;

    &__modal-content {
      text-align: center;
      padding: 30px;
      border-radius: 25px;
      border: 2px solid;
      background-color: var(--grey85);
      max-width: 90%;
      max-height: 90%;
      overflow-y: auto;
      position: relative;
      z-index: 1001;
    }

    &__exit-icon {
      position: absolute;
      width: 30px;
      top: 10px;
      right: 10px;

      &:hover {
        cursor: pointer;
        transform: scale(1.3);
      }
    }
    &__header {
      margin: 0 10px 0 0;
    }

    &__line {
      width: 100%;
      height: 2px;
      background-color: black;
    }
  }
</style>
