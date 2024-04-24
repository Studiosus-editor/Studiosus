<script>
  import Projects from "../../assets/svg/Nav-controller/Projects.svg";
  import { projectExpanded } from "./scripts/store.js";
  function handleExpandBtn() {
    projectExpanded.update((value) => !value);
  }
</script>

<div id="nav-controller">
  <button
    title="Explorer"
    id="projectExpand-btn"
    class="nav-buttons"
    on:click={handleExpandBtn}
  >
    <img src={Projects} alt="Projects" width="40px" height="40px" />
  </button>
</div>

<style>
  #nav-controller {
    position: relative;
    z-index: 3;
    display: flex;
    width: 50px;
    background-color: var(--grey85);
    border-right: 2px solid var(--silver);
  }
  .nav-buttons {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    align-items: center;
    background: transparent;
    border: none;
    width: 50px;
    height: 50px;
  }
  .nav-buttons:hover {
    background: var(--silver);
    cursor: pointer;
  }
</style>
