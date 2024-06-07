<script>
  import { _ } from "svelte-i18n";
  import copyToClipboardIcon from "../../../assets/svg/copy-to-clipboard.svg";
  import { addToast } from "../../Modal/ToastNotification/toastStore.js";

  export let showHeader = true;
  export let viewerInviteLink;
  export let editorInviteLink;

  const backendUrl = __BACKEND_URL__;

  function copyToClipboard(text) {
    navigator.clipboard.writeText(text);
    addToast({
      message: $_("modalAddByLinkBox.copiedToClipboard"),
      type: "info",
    });
  }
</script>

<div class="link-section">
  {#if showHeader}
    <h3 class="link-section__header">{$_("modalAddByLinkBox.addByLink")}</h3>
  {/if}
  <form disabled>
    <h3 class="link-section__label-wrapper">
      <label for="vlink">{$_("modalAddByLinkBox.viewerLink")}</label>
    </h3>
    <div class="link-section__link-wrapper">
      <input
        type="text"
        id="vlink"
        name="vlink"
        disabled
        value={backendUrl +
          "/api/project/invite?viewerLink=" +
          viewerInviteLink}
        placeholder="place for link"
      />
      <img
        src={copyToClipboardIcon}
        alt={$_("modalAddByLinkBox.altCopyToClipBoard")}
        on:click={() =>
          copyToClipboard(
            backendUrl + "/api/project/invite?viewerLink=" + viewerInviteLink
          )}
        on:keydown={() =>
          copyToClipboard(
            backendUrl + "/api/project/invite?viewerLink=" + viewerInviteLink
          )}
      />
    </div>
    <h3 class="link-section__label-wrapper">
      <label for="elink">{$_("modalAddByLinkBox.editorLink")}</label>
    </h3>
    <div class="link-section__link-wrapper">
      <input
        type="text"
        id="elink"
        name="elink"
        disabled
        value={backendUrl +
          "/api/project/invite?editorLink=" +
          editorInviteLink}
        placeholder="place for link"
      />
      <img
        src={copyToClipboardIcon}
        alt={$_("modalAddByLinkBox.altCopyToClipBoard")}
        on:click={() =>
          copyToClipboard(
            backendUrl + "/api/project/invite?editorLink=" + editorInviteLink
          )}
        on:keydown={() =>
          copyToClipboard(
            backendUrl + "/api/project/invite?editorLink=" + editorInviteLink
          )}
      />
    </div>
  </form>
</div>

<style lang="scss">
  .link-section {
    display: flex;
    flex-direction: column;
    text-align: left;
    margin-bottom: 24px;

    input {
      background-color: var(--white);
      width: 85%;
      margin-right: 20px;
      text-align: left;
      padding-left: 5px;
    }

    &__header {
      margin-bottom: 0px;
      text-align: center;
    }
    &__label-wrapper {
      text-align: left;
    }
    &__link-wrapper {
      display: flex;
      align-items: center;

      img:hover {
        cursor: pointer;
        transition: transform 0.05s ease;
        transform: scale(1.2);
      }
    }
  }
  input {
    height: 40px;
    text-align: center;
    font-family: "Rubik", sans-serif;
    font-weight: 400;
    font-size: 24px;
    box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.3);
    border-radius: 5px;
    border: 1px solid;
  }

  h3 {
    font-weight: 500;
    margin-top: 41px;
  }
  img {
    width: 35px;
  }
</style>
