<script>
  import AddIcon from "../../assets/svg/add-plus-icon.svg";
  import { _ } from "svelte-i18n";
</script>

<div class="project-wrapper">
  <div class="project-wrapper__name">
    <p>{$_("projects.createProject")}</p>
  </div>
  <div class="project-wrapper__play">
    <img src={AddIcon} alt={$_("projects.addNew")} />
  </div>
</div>

<style lang="scss">
  .project-wrapper {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: var(--egg-white);
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
  }
</style>
