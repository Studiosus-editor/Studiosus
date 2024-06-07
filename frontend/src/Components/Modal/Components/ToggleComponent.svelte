<script>
  import { onMount } from "svelte";
  import { createEventDispatcher } from "svelte";
  import { _ } from "svelte-i18n";

  export let firstName;
  export let secondName;
  export let firstActive = true;
  export let secondActive = false;
  export let pending = false;
  export let isUpdateComponent = false;
  export let isReviewComponent = false;

  const FIRST_SELECTED = 1;
  const SECOND_SELECTED = 2;

  const dispatch = createEventDispatcher();

  let firstToggleElement;
  let secondToggleElement;

  $: reviewActiveDeny =
    isReviewComponent && firstActive ? "review-active deny" : "";

  $: reviewActiveApprove =
    isReviewComponent && secondActive ? "review-active approve" : "";

  onMount(() => {
    firstToggleElement = document.querySelector(
      ".toggle-wrapper__toggle-first"
    );
    secondToggleElement = document.querySelector(
      ".toggle-wrapper__toggle-second"
    );
  });

  // handles toggling logic to apply active class
  function toggle(event) {
    const element = event.currentTarget;
    const isFirst = element.classList.contains("toggle-wrapper__toggle-first");
    const isSecond = element.classList.contains(
      "toggle-wrapper__toggle-second"
    );

    // Does nothing if the active toggle is clicked
    if ((isFirst && firstActive) || (isSecond && secondActive)) {
      return;
    }
    if (isFirst) {
      firstActive = true;
      secondActive = false;
    } else {
      firstActive = false;
      secondActive = true;
    }
    dispatch("toggle", {
      selection: firstActive ? FIRST_SELECTED : SECOND_SELECTED,
    });
  }
</script>

<div class="toggle-wrapper">
  <div
    class="toggle-wrapper__toggle-first toggle {reviewActiveDeny}"
    class:active={firstActive}
    on:click={toggle}
    on:keydown={toggle}
  >
    <h4>{firstName}</h4>
  </div>
  <div
    class="toggle-wrapper__toggle-second toggle {reviewActiveApprove}"
    class:active={secondActive}
    on:click={toggle}
    on:keydown={toggle}
  >
    <h4>{secondName}</h4>
  </div>
  {#if pending && isUpdateComponent}
    <div class="pending">{$_("toggleComponent.invited")}</div>
  {/if}
</div>

<style lang="scss">
  .toggle-wrapper {
    display: flex;

    div {
      display: flex;
      justify-content: center;
      align-items: center;
    }

    h4 {
      margin: 5px 0 5px 0;
    }

    &__toggle-first {
      border-radius: 5px 0 0 5px;
      border-right: none !important;
    }
    &__toggle-second {
      border-radius: 0 5px 5px 0;
      border-left: none !important;
    }
  }

  .toggle {
    width: 50%;
    background-color: var(--grey85);
    border: 1px solid black;

    &:hover {
      cursor: pointer;
    }
  }

  .active {
    background-color: var(--periwinkle);
    box-shadow:
      0 4px 8px rgba(0, 0, 0, 0.1),
      0 6px 20px rgba(0, 0, 0, 0.1);
    transform: scale(1.05);

    &:hover {
      cursor: default;
    }
  }
  .review-active {
    box-shadow:
      0 4px 8px rgba(0, 0, 0, 0.1),
      0 6px 20px rgba(0, 0, 0, 0.1);
    transform: scale(1.05);

    &:hover {
      cursor: default;
    }
  }

  .deny {
    background-color: var(--mandy);
  }

  .approve {
    background-color: var(--medium-sea-green);
  }

  .pending {
    font-family: "Montserrat", sans-serif;
    font-weight: bolder;
    font-size: 1.2rem;
    margin-left: 10px;
    width: 50%;
    color: var(--hippie-blue);
  }
</style>
