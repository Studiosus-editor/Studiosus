<script>
  import { _, locale } from "svelte-i18n";
  import ArrowDown from "../../assets/svg/dropdown-arrow-icon-white.svg";
  import { AVAILABLE_LOCALES, changeLocale } from "../../services/i18n.js";
  import { onMount } from "svelte";

  let langDropdownButtonText;
  let langDropdownVisible = false;

  // toggles the display of the language dropdown menu + arrow rotation on user click
  function handleLangDropdownClick() {
    langDropdownVisible = !langDropdownVisible;
  }

  // Changes the language displayed in the language dropdown button
  function handleLanguageSelect(event) {
    const selectedLanguage = event.target.textContent;
    langDropdownButtonText.textContent = selectedLanguage;
    changeLocale(selectedLanguage);
  }

  function closeDropdown() {
    if (langDropdownVisible) {
      langDropdownVisible = false;
    }
  }
  onMount(() => {
    langDropdownButtonText = document.querySelector(".dropdown--btn h3");
    document.addEventListener("close-dropdown", closeDropdown);
  });
</script>

<div
  class="dropdown"
  on:click={handleLangDropdownClick}
  on:keydown={handleLangDropdownClick}
>
  <button
    type="button"
    class="dropdown--btn button--default"
    class:active={langDropdownVisible}
  >
    {#if $locale !== "lt"}
      <h3>{$locale}</h3>
    {/if}
    {#if $locale !== "en"}
      <h3>{$locale}</h3>
    {/if}
    <img
      id="dropdown__arrow-icon"
      src={ArrowDown}
      class:active={langDropdownVisible}
      alt="toggle language select"
    />
  </button>
  <div class="dropdown__dropdown-content" class:visible={langDropdownVisible}>
    {#each Object.values(AVAILABLE_LOCALES) as item}
      <div
        class="dropdown__item-container"
        on:click={handleLanguageSelect}
        on:keydown={handleLanguageSelect}
      >
        <h3>{item}</h3>
      </div>
    {/each}
  </div>
</div>

<style lang="scss">
  .dropdown {
    &--btn {
      display: flex;
      align-items: center;
      padding: 18px 15px 17px 15px;
      width: 80px;
      text-transform: uppercase;

      h3 {
        margin: 0;
      }

      &:hover {
        background-color: var(--maastricht-blue);
        box-shadow:
          rgba(0, 0, 0, 0.05) 0 5px 30px,
          rgba(0, 0, 0, 0.05) 0 1px 4px;
        opacity: 1;
        transform: translateY(0);
        transition-duration: 0.35s;
      }

      &.active {
        background-color: var(--maastricht-blue) !important;
      }

      img {
        width: 35px;
        transition: transform 0.3s ease;

        &.active {
          transform: rotate(90deg);
        }
      }
    }

    &__dropdown-content {
      display: none;
      position: absolute;
      background-color: var(--maastricht-blue);
      min-width: 75px;
      box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.5);
      z-index: 10;

      &.visible {
        display: block;
      }
    }

    &__item-container {
      text-align: center;

      :hover {
        background-color: var(--hippie-blue);
        box-shadow:
          rgba(0, 0, 0, 0.05) 0 5px 30px,
          rgba(0, 0, 0, 0.05) 0 1px 4px;
        opacity: 1;
        transform: translateY(0);
        transition-duration: 0.35s;
        cursor: pointer;
      }

      h3 {
        text-transform: uppercase;
        margin: 0;
        padding: 24.5px;
      }
    }
  }
</style>
