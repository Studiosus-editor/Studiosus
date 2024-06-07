<script>
  import SearchIcon from "../../assets/svg/arrow-right-icon.svg";
  import { clearableInput } from "../../utils/clearableInput.js";
  import { addToast } from "../Modal/ToastNotification/toastStore.js";
  import { createEventDispatcher } from "svelte";
  import { _ } from "svelte-i18n";

  const dispatch = createEventDispatcher();

  let searchOption = "title";
  let searchValue = "";

  let searchValueUsername = "";
  let searchValueTitle = "";

  const backendUrl = __BACKEND_URL__;

  function handleSearch() {
    if (searchOption === "title") {
      searchValueTitle = searchValue;
      searchValueUsername = "";
    } else if (searchOption === "username") {
      searchValueUsername = searchValue;
      searchValueTitle = "";
    }
    getSearchResults();
  }

  async function getSearchResults() {
    return fetch(backendUrl + "/api/templates/search", {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify({
        title: searchValueTitle,
        username: searchValueUsername,
      }),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        dispatch("searchResults", data);
      })
      .catch((error) => {
        addToast({
          message: $_("templatesSearchField.errorSearching"),
          type: "error",
        });
      });
  }
</script>

<form class="search-field" on:submit|preventDefault>
  <input
    type="text"
    placeholder={$_("templatesSearchField.search")}
    use:clearableInput
    bind:value={searchValue}
  />
  <select bind:value={searchOption}>
    <option value="title">{$_("templatesSearchField.byTitle")}</option>
    <option value="username">{$_("templatesSearchField.byUsername")}</option>
  </select>
  <button class="button--default" on:click={handleSearch}>
    <img src={SearchIcon} alt={$_("templatesSearchField.altSearch")} />
  </button>
</form>

<style lang="scss">
  .search-field {
    display: flex;
    align-items: center;
    height: 40px;
    box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.3);
    min-width: 300px;
    input {
      height: 100%;
      text-align: left;
      font-family: "Rubik", sans-serif;
      font-weight: 400;
      font-size: 24px;
      border-top-left-radius: 5px;
      border-bottom-left-radius: 5px;
      border: 1px solid;
      border-right: none;
      padding-left: 5px;
      min-width: 100px;
    }

    select {
      background-color: var(--whisper);
      height: 44px;
      font-family: "Rubik", sans-serif;
      font-weight: 400;
      font-size: 16px;
      border: 1px solid;
      border-left: none;
      border-right: none;

      &:hover {
        cursor: pointer;
        background-color: var(--grey85);
      }

      &:focus {
        background-color: var(--whisper);
      }
    }

    button {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 15%;
      background-color: var(--periwinkle);
      border-top-right-radius: 5px;
      border-bottom-right-radius: 5px;
      border: 1px solid;
      border-left: none;
      cursor: pointer;

      &:hover {
        background-color: var(--spindle);
      }

      img {
        width: 40px;
        flex-shrink: 0;
        height: 100%;
      }
    }
  }
</style>
