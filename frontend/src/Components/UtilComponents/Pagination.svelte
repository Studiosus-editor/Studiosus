<script>
  import { createEventDispatcher } from "svelte";
  import ArrowLeft from "../../assets/svg/arrow-left-icon.svg";
  import ArrowRight from "../../assets/svg/arrow-right-icon.svg";
  import { _ } from "svelte-i18n";

  export let itemsPerPage;
  export let itemArray;
  export let currentPage = 1;

  let showNextButton;
  const dispatch = createEventDispatcher();

  function getStartIndex() {
    return (currentPage - 1) * itemsPerPage;
  }

  function getEndIndex() {
    return currentPage * itemsPerPage;
  }

  export function getCurrentPageItems() {
    return itemArray.slice(getStartIndex(), getEndIndex());
  }

  function goToPage(page) {
    currentPage = page;
    dispatch("pageChange", getCurrentPageItems());
  }

  function hasMoreItems() {
    return getEndIndex() < itemArray.length;
  }

  // reactive statment that updates showNextButton variable when currentPage changes
  $: (showNextButton = hasMoreItems()), [currentPage, itemArray];
</script>

<div class="paggination-wrapper">
  {#if itemArray.length > itemsPerPage}
    <button
      class="button--default"
      on:click={() => goToPage(currentPage - 1)}
      class:hide-button={currentPage === 1}
      disabled={currentPage === 1}
    >
      <img
        src={ArrowLeft}
        alt={$_("paggination.previous")}
        class:hidden={currentPage === 1}
      />
    </button>
    <h3 class="paggination-wrapper__current-page">{currentPage}</h3>
    <button
      class="button--default"
      on:click={() => goToPage(currentPage + 1)}
      class:hide-button={!showNextButton}
      disabled={!showNextButton}
    >
      <img
        src={ArrowRight}
        alt={$_("paggination.next")}
        class:hidden={!showNextButton}
      />
    </button>
  {/if}
</div>

<style lang="scss">
  .paggination-wrapper {
    display: flex;
    align-items: center;

    & button {
      width: 40px;

      & img {
        width: 30px;
      }
    }

    &__current-page {
      margin: 0 10px 10px 10px;
    }
  }

  .hidden {
    display: none;
  }

  .hide-button {
    cursor: default;
  }
</style>
