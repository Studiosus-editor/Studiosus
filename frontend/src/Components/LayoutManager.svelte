<script>
  export let toastNotifications;
  export let header;
  export let footer;

  let showFooter = true;
  let pageClass = "";

  $: {
    const currentHref = window.location.href;
    showFooter = !/\/editor|\/project\/[^/]+|\/$/.test(currentHref);
    pageClass = /\/editor/.test(currentHref) ? "editor" : "other";
    if (/\/$/.test(currentHref)) {
      pageClass = "home";
    }
  }
</script>

<svelte:component this={toastNotifications} />
<div class={pageClass + " content-wrapper"}>
  <div class="header"><svelte:component this={header} /></div>
  <main>
    <slot />
  </main>
  {#if showFooter}
    <div class="footer"><svelte:component this={footer} /></div>
  {/if}
</div>

<style>
  .content-wrapper {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }

  .home.content-wrapper {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }

  .editor.content-wrapper {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }

  .other.content-wrapper {
    justify-content: space-between;
  }

  main {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }

  @media (max-width: 920px) {
    main {
      margin: 0;
    }
    .header {
      height: 70px;
    }
  }
</style>
