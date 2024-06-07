<script>
  import { _ } from "svelte-i18n";
  import ViewTemplateIcon from "../../assets/svg/arrow-right-icon.svg";
  import AddTemplateIcon from "../../assets/svg/add-plus-icon.svg";
  import InfoIcon from "../../assets/svg/info-icon.svg";
  import DeleteIcon from "../../assets/svg/exit-cross-black.svg";
  import ReviewIcon from "../../assets/svg/review-icon.svg";
  import AdminCommentIcon from "../../assets/svg/admin-comment-icon.svg";
  import { onMount, createEventDispatcher } from "svelte";

  export let id;
  export let name;
  export let username = null;
  export let description;
  export let isAdmin = false;
  export let isGlobalTemplate = false;
  export let isLoggedIn = true;
  export let phase = "";
  export let reviewComment = "";

  let phaseColorClass = "";
  let truncatedDescription;
  const OVERVIEW_DESC_LENGTH = 200;
  const dispatch = createEventDispatcher();

  onMount(() => {
    truncatedDescription = truncateDescription();
    if (phase === "APPROVED") {
      phaseColorClass = "approved";
    } else if (phase === "REJECTED") {
      phaseColorClass = "rejected";
    } else if (phase === "PENDING") {
      phaseColorClass = "pending";
    }
  });

  function truncateDescription() {
    return description.length > OVERVIEW_DESC_LENGTH
      ? description.substring(0, OVERVIEW_DESC_LENGTH) + "..."
      : description;
  }

  function handleTempalteDelete() {
    dispatch("deleteTemplate", { id: id, name: name, phase: phase });
  }

  function handleViewResponseComment() {
    dispatch("viewResponseComment", { comment: reviewComment });
  }

  function handleInformationClick() {
    dispatch("informationClick", {
      name,
      username,
      description,
    });
  }
  function handleReviewClick() {
    dispatch("reviewClick", {
      name,
      username,
      description,
      id,
    });
  }

  function previewTemplate() {
    window.location.href = `/template/${id}`;
  }

  function handleTemplateCopyToProject() {
    dispatch("copyToProject", { id: id, name: name });
  }
</script>

<div class="template">
  {#if !username && !isAdmin && !isGlobalTemplate}
    <img
      title={$_("templateEntry.deleteTemplate")}
      class="template__delete-icon"
      src={DeleteIcon}
      alt={$_("templateEntry.deleteTemplate")}
      on:click={handleTempalteDelete}
      on:keydown={handleTempalteDelete}
    />
  {/if}
  {#if reviewComment}
    <img
      title={$_("templateEntry.checkReviewComments")}
      class="template__admin-comment-icon"
      src={AdminCommentIcon}
      alt={$_("templateEntry.checkReviewComments")}
      on:click={handleViewResponseComment}
      on:keydown={handleViewResponseComment}
    />
  {/if}
  <div class="template__name">
    <h3>{name}</h3>
  </div>
  {#if username}
    <div class="template__username">
      <h4>{$_("templateEntry.createdBy")}{" " + username}</h4>
    </div>
  {:else if phase}
    <div class="template__status">
      <h4 class={phaseColorClass}>{phase}</h4>
    </div>
  {/if}
  <div class="template__description">
    <p>{truncatedDescription}</p>
  </div>
  <div class="template__buttons">
    <button
      class="button--default"
      title={$_("templateEntry.viewDetailedInformation")}
      on:click={handleInformationClick}
      on:keydown={handleInformationClick}
    >
      <img src={InfoIcon} alt={$_("templateEntry.viewDetailedInformation")} />
    </button>
    {#if isAdmin}
      <button
        class="button--default"
        title={$_("templateEntry.reviewTemplate")}
        on:click={handleReviewClick}
        on:keydown={handleReviewClick}
      >
        <img src={ReviewIcon} alt={$_("templateEntry.reviewTemplate")} />
      </button>
    {/if}
    {#if username && isLoggedIn}
      <button
        class="button--default"
        title={$_("templateEntry.addToYourProjects")}
        on:click={handleTemplateCopyToProject}
        on:keydown={handleTemplateCopyToProject}
      >
        <img
          src={AddTemplateIcon}
          alt={$_("templateEntry.addToYourProjects")}
        />
      </button>
    {/if}
    <button
      class="button--default"
      title={$_("templateEntry.previewTemplate")}
      on:click={previewTemplate}
    >
      <img src={ViewTemplateIcon} alt={$_("templateEntry.previewTemplate")} />
    </button>
  </div>
</div>

<style lang="scss">
  .template {
    display: flex;
    max-width: 100%;
    height: 250px;
    padding: 20px 30px 0 20px;
    border-radius: 5px;
    border: 1px solid;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;

    &__delete-icon {
      position: absolute;
      width: 30px;
      top: 10px;
      right: 10px;
      &:hover {
        cursor: pointer;
        transform: scale(1.3);
      }
    }

    &__admin-comment-icon {
      position: absolute;
      width: 30px;
      top: 10px;
      left: 10px;
      &:hover {
        cursor: pointer;
        transform: scale(1.3);
      }
    }

    &__description {
      width: 100%;
      height: 100%;
      p {
        text-align: left;
        white-space: normal;
        overflow-wrap: break-word;
      }
    }

    &__buttons {
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;

      & button {
        img {
          width: 30px;
          height: 30px;
          &:hover {
            cursor: pointer;
            transform: scale(1.3);
          }
        }
      }
    }
    &__name {
      width: 100%;
      word-wrap: break-word;

      h3 {
        margin: 0;
        color: var(--dark-cerulean);
      }
    }
    &__username {
      width: 100%;
      word-wrap: break-word;

      h4 {
        margin: 0;
      }
    }

    &__status {
      width: 100%;

      h4 {
        margin: 0;
      }
    }
  }

  .approved {
    color: var(--medium-sea-green);
  }
  .pending {
    color: var(--maastricht-blue);
  }
  .rejected {
    color: var(--bittersweet);
  }
  @media (max-width: 980px) {
    .template {
      height: 280px;
    }
  }
  @media (max-width: 650px) {
    .template {
      height: 220px;
    }
  }
</style>
