<script>
  import PlayIcon from "../../assets/svg/play-icon.svg";
  import EditIcon from "../../assets/svg/project-edit-icon.svg";
  import ViewIcon from "../../assets/svg/project-view-icon.svg";
  import { _ } from "svelte-i18n";

  export let projectName;
  export let isActive;
  export let toggleActive;
  export let projectId;
  export let role;

  function openProject() {
    window.location.href = `/project/${projectId}/${role}`;
  }
</script>

<div
  class="project-wrapper"
  class:active={isActive}
  on:click={toggleActive}
  on:keydown={toggleActive}
>
  <div class="project-wrapper__name">
    <p>{projectName}</p>
  </div>
  <div class="project-wrapper__right">
    {#if role == "EDITOR"}
      <img
        class="project-wrapper__edit-icon"
        src={EditIcon}
        alt={$_("projectEntry.editor")}
        title={$_("projectEntry.editor")}
      />
    {:else if role == "VIEWER"}
      <img
        class="project-wrapper__view-icon"
        src={ViewIcon}
        alt={$_("projectEntry.viewer")}
        title={$_("projectEntry.viewer")}
      />
    {/if}
    <div class="project-wrapper__play">
      <button class="button--default" on:click={openProject}>
        <img
          src={PlayIcon}
          alt={$_("projectEntry.open")}
          title={$_("projectEntry.open")}
        />
      </button>
    </div>
  </div>
</div>

<style lang="scss">
  .project-wrapper {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: var(--grey85);
    width: 40%;
    min-width: 300px;
    margin: 10px;
    padding: 5px 10px;
    box-shadow: 0px 8px 8px 0px rgba(0, 0, 0, 0.3);
    border: 1px solid transparent;

    &__right {
      display: flex;
      align-items: center;
      img {
        margin-right: 10px;
      }
    }
    &__edit-icon {
      width: 25px;
    }
    &__view-icon {
      width: 30px;
    }

    button {
      padding: 0;
    }

    &__name {
      max-width: 70%;

      p {
        margin: 0;
        font-family: "Montserrat", sans-serif;
        font-weight: 400;
        font-size: 20px;
        text-overflow: ellipsis;
        overflow: hidden;
        max-width: 100%;
      }
    }

    &__play img {
      width: 40px;
    }

    &:hover {
      cursor: pointer;
      border: 1px solid black;
    }
    &.active {
      border: 1px solid black;
      cursor: default;
      background-color: var(--periwinkle);
    }
  }
</style>
