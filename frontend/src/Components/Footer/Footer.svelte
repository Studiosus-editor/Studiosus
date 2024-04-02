<script>
  import { onMount } from 'svelte';
  import LanguageBox from './LanguageBox.svelte';
  import LtFlag from "../../assets/svg/lithuania-flag-icon.svg";
  import EnFlag from "../../assets/svg/united-kingdom-flag-icon.svg";
  import { _, locale }  from 'svelte-i18n';
  import { changeLocale } from '../../services/i18n.js';

  let currentYear = new Date().getFullYear();

  onMount(() => {
    // Updates the current year in the footer
    const yearElement = document.getElementById('copyright');
    if (yearElement) {
      yearElement.textContent = currentYear + " © Studiosus";
    }
  });

  function handleLanguageSelect(language) {
    changeLocale(language);
  }
</script>

<footer>
  <div class="footer-container">
    <p id="copyright">{currentYear}</p>
  </div>
  <div class="language-container">
    {#if $locale !== 'lt'}
      <LanguageBox flagIcon={LtFlag} languageName={$_('footer.LT')} onClick={() => handleLanguageSelect('lt')}/>
    {/if}
    {#if $locale !== 'en'}
      <LanguageBox flagIcon={EnFlag} languageName={$_('footer.EN')} onClick={() => handleLanguageSelect('en')}/>
    {/if}
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

  .language-container {
    display: flex;
    justify-content: center;
  }
</style>
