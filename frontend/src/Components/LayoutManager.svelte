<script>
  export let header;
  export let footer;
</script>

<div class="content-wrapper">
  <div class="header"><svelte:component this={header} /></div>
  <main>
    <slot />
  </main>
  <div class="footer"><svelte:component this={footer} /></div>
</div>

<style>
  .content-wrapper {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-height: 100vh;
  }

  main {
    margin: 0 5%;
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
      height: 86px;
    }
  }
</style>
