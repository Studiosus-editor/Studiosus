<script>
  export let projectName;
  export let isActive;
  export let toggleActive;

  import PlayIcon from "../../assets/svg/play-icon.svg";
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
  <div class="project-wrapper__play">
    <img src={PlayIcon} alt="Launch" />
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

    &__name p {
      margin: 0;
      font-family: "Montserrat", sans-serif;
      font-weight: 400;
      font-size: 20px;
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
