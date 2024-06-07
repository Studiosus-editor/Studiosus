<script>
  import { _ } from "svelte-i18n";
  import { createEventDispatcher, onMount } from "svelte";
  import { addToast } from "../../Modal/ToastNotification/toastStore.js";
  import ToggleComponent from "./ToggleComponent.svelte";

  export let name = "";
  export let ownerName = "";
  export let description;
  export let isAdmin = false;
  export let templateId = null;

  const MAX_TEMPLATE_DESC = 600;
  const backendUrl = __BACKEND_URL__;

  let verdict = "";
  let reviewComment;

  const dispatch = createEventDispatcher();

  function handleClose() {
    dispatch("closeInformationModal");
  }

  async function handleReviewSubmit() {
    const verdictValue = verdict == 1 ? "REJECTED" : "APPROVED";
    return fetch(backendUrl + `/api/template/${templateId}:review`, {
      method: "PUT",
      headers: {
        Accept: "text/plain",
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify({
        phase: verdictValue,
        comment: reviewComment,
      }),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      })
      .then(() => {
        addToast({
          message: $_("templateViewOrReview.reviewSubmittedSuccessfully"),
          type: "success",
        });
        localStorage.removeItem("templateReview-" + templateId);
        dispatch("refreshReviewTemplates");
        dispatch("closeReviewModal");
      });
  }

  function handleReviewToggle(event) {
    verdict = event.detail.selection;
  }

  function saveCurrentComment() {
    localStorage.setItem("templateReview-" + templateId, reviewComment);
  }

  onMount(() => {
    if (isAdmin) {
      const storedComment = localStorage.getItem(
        "templateReview-" + templateId
      );
      if (storedComment) {
        reviewComment = storedComment;
      }
    }
  });
</script>

<div class="content-wrapper">
  <div class="content-wrapper__name-wrapper">
    <h3>{name}</h3>
    {#if ownerName}
      <h4>
        {$_("templateViewOrReview.createdBy")}
        {" " + ownerName}
      </h4>
    {/if}
  </div>
  <div class="content-wrapper__description-container">
    <textarea
      disabled
      id="templateDescription"
      name="templateDescription"
      placeholder={$_("templateViewOrReview.noDescriptionProvided")}
      bind:value={description}
    ></textarea>
  </div>
  {#if !isAdmin}
    <div class="content-wrapper__button-container">
      <button class="button--blue" on:click={handleClose}
        >{$_("templateViewOrReview.close")}</button
      >
    </div>
  {/if}
  {#if isAdmin}
    <div class="content-wrapper__review-container">
      <ToggleComponent
        firstName={$_("templateViewOrReview.deny")}
        secondName={$_("templateViewOrReview.approve")}
        isReviewComponent={true}
        firstActive={false}
        on:toggle={handleReviewToggle}
      />
    </div>
    {#if verdict == 1}
      <div class="content-wrapper__description-container">
        <textarea
          id="templateComment"
          name="templateComment"
          maxlength={MAX_TEMPLATE_DESC}
          on:blur={(event) => {
            if (event.target.value === "") {
              event.target.placeholder = $_(
                "templateViewOrReview.denyDescriptionPlaceholder"
              );
            }
          }}
          placeholder={$_("templateViewOrReview.denyDescriptionPlaceholder")}
          on:change={saveCurrentComment}
          bind:value={reviewComment}
        ></textarea>
      </div>
    {/if}
    <div class="content-wrapper__button-container">
      <button
        disabled={!verdict || (!reviewComment && verdict == 1)}
        class="button--blue"
        style={verdict == 1 ? "margin-top: 24px;" : ""}
        on:click={handleReviewSubmit}
        >{$_("templateViewOrReview.submit")}</button
      >
    </div>
  {/if}
</div>

<style lang="scss">
  .content-wrapper {
    margin-top: 25px;
    display: flex;
    flex-direction: column;
    align-items: center;

    &__review-container {
      margin-top: 24px;
      margin-bottom: 24px;
      width: 100%;
    }

    &__name-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 24px;
      h4 {
        margin: 0;
      }
      h3 {
        margin: 0;
      }
    }
    &__button-container {
      margin-top: 24px;
      width: 30%;

      button:disabled {
        opacity: 0.5;
        color: black;
        cursor: default;

        &:hover {
          background-color: var(--periwinkle);
        }
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
