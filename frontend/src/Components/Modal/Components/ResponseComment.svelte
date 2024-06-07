<script>
  import { _ } from "svelte-i18n";
  import { createEventDispatcher } from "svelte";

  export let responseComment;

  const dispatch = createEventDispatcher();

  function handleClose() {
    dispatch("closeResponseCommentModal");
  }
</script>

<div class="content-wrapper">
  <div class="content-wrapper__info-wrapper">
    <h3>{$_("responseCommentModal.sorryYourTemplateWasDenied")}</h3>
  </div>
  <div class="content-wrapper__description-container">
    <textarea
      disabled
      id="reviewComment"
      name="reviewComment"
      placeholder={$_("responseCommentModal.reviewPlaceholder")}
      bind:value={responseComment}
    ></textarea>
  </div>
  <div class="content-wrapper__button-container">
    <button class="button--blue" on:click={handleClose}
      >{$_("responseCommentModal.close")}</button
    >
  </div>
</div>

<style lang="scss">
  .content-wrapper {
    margin-top: 25px;
    display: flex;
    flex-direction: column;
    align-items: center;

    &__info-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 24px;
      h3 {
        margin: 0;
      }
    }
    &__button-container {
      margin-top: 24px;
      width: 30%;

      button:hover {
        background-color: var(--periwinkle);
      }
    }
    &__description-container {
      width: 100%;

      textarea {
        padding-left: 5px;
        width: 100%;
        height: 200px;
        font-family: "Rubik", sans-serif;
        font-weight: 400;
        font-size: 18px;
        box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.3);
        border-radius: 5px;
        border: 1px solid;
        resize: none;

        &:disabled {
          background-color: white;
          color: black;
        }

        &::-webkit-scrollbar {
          width: 10px;
        }
        &::-webkit-scrollbar-track {
          background: var(--grey85);
        }

        &::-webkit-scrollbar-thumb {
          background-color: var(--periwinkle);
          border-radius: 5px;
          border: 1px solid var(--black);
          border-right: none;
        }
      }
    }
  }
</style>
