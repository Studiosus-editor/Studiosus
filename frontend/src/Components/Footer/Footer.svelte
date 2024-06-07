<script>
  import { onMount } from "svelte";

  let currentYear = new Date().getFullYear();

  onMount(() => {
    // Updates the current year in the footer
    const yearElement = document.getElementById("copyright");
    if (yearElement) {
      yearElement.textContent = currentYear + " © Studiosus";
    }
  });
</script>

<footer>
  <div class="footer-container">
    <p id="copyright">{currentYear}</p>
  </div>
</footer>

<style lang="scss">
  footer {
    display: flex;
    flex-direction: column;
  }
  .footer-container {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    height: 86px;
    background-color: var(--dark-cerulean);

    & p {
      color: var(--white);
      font-family: "Rubik", sans-serif;
      margin-right: 25px;
    }
  }
</style>
