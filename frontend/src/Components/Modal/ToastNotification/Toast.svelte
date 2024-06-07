<script>
  import { createEventDispatcher } from "svelte";
  import { fade } from "svelte/transition";
  import SuccessIcon from "./Icons/SuccessIcon.svelte";
  import ErrorIcon from "./Icons/ErrorIcon.svelte";
  import InfoIcon from "./Icons/InfoIcon.svelte";
  import CloseIcon from "./Icons/CloseIcon.svelte";

  const dispatch = createEventDispatcher();

  export let type = "error";
  export let dismissible = true;
</script>

<article class={type} role="alert" transition:fade>
  {#if type === "success"}
    <SuccessIcon width="25px" />
  {:else if type === "error"}
    <ErrorIcon width="25px" />
  {:else}
    <InfoIcon width="25px" />
  {/if}

  <div class="text">
    <slot />
  </div>

  {#if dismissible}
    <button class="close" on:click={() => dispatch("dismiss")}>
      <CloseIcon width="0.8em" />
    </button>
  {/if}
</article>

<style>
    article {
        font-weight: bolder;
        font-family: "Montserrat", sans-serif;
        padding: 0.75rem 1.5rem;
        border-radius: 0.2rem;
        box-shadow: 0px 8px 8px 0px rgba(0, 0, 0, 0.3);
        display: flex;
        align-items: center;
        margin: 0 auto 0.5rem auto;
        width: 20rem;
    }
    .error {
        background: var(--mandy);
    }
    .success {
        background: var(--medium-sea-green);
    }
    .info {
        background: var(--periwinkle);
    }
    .text {
        margin-left: 1rem;
    }
    button {
        color: white;
        background: transparent;
        border: 0 none;
        padding: 0;
        margin: 0 0 0 auto;
        line-height: 1;
        font-size: 1rem;
    }

    button:hover {
        cursor: pointer;
    }
</style>
