<script>
  import { onMount } from "svelte";
  import { _ } from "svelte-i18n";
  import CreateProject from "./CreateProject.svelte";
  import Pagination from "./Pagination.svelte";
  import ProjectEntry from "./ProjectEntry.svelte";

  const backendUrl = __BACKEND_URL__;
  let errorLoadingProjects = false;
  let projectsActive = true;
  let sharedProjectsActive = false;
  const PROJECTS_PER_PAGE = 8;
  let currentlyDisplayedYourProjects = [];
  let currentlyDisplayedSharedProjects = [];

  // tracks if projects are loading, used for conditional rendering
  let isLoading = true;

  let yourProjects = [];

  let sharedProjects = [];

  // artifical delay for databse fetchig DELETE AFTER IMPLEMENTING DATABASE
  async function fetchProjects() {
    return fetch(backendUrl + "/api/projects", {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        return null;
      });
  }

  async function loadAndDisplayProjects() {
    yourProjects = [];
    sharedProjects = [];
    isLoading = true;
    const fetchedProjects = await fetchProjects();
    if (fetchedProjects === null) {
      isLoading = false;
      errorLoadingProjects = true;
      return;
    }

    fetchedProjects.forEach((project) => {
      if (project.role === "Owner") {
        yourProjects.push(project);
      } else {
        sharedProjects.push(project);
      }
    });
    // slice on page load so only the first projects are shown and the rest are paginated
    if (projectsActive) {
      currentlyDisplayedYourProjects = yourProjects.slice(0, PROJECTS_PER_PAGE);
    } else {
      currentlyDisplayedSharedProjects = sharedProjects.slice(
        0,
        PROJECTS_PER_PAGE
      );
    }
    // sets the first element as active, since the projects will be sorted by most recent
    if (!isEmptyArray(yourProjects)) {
      yourProjects[0].isActive = true;
    }
    isLoading = false;
  }

  // toggles the active class on the project that was clicked & unsets the rest
  function toggleProjectActive(id, projectsArray) {
    return projectsArray.map((project) => ({
      ...project,
      isActive: project.id === id,
    }));
  }

  // toggles the active class on nav items (Your Projects, Shared with you), as well as defaults to first page when tabs are switched
  function toggleActive(item) {
    if (item === "projects" && !projectsActive) {
      projectsActive = true;
      sharedProjectsActive = false;
      currentlyDisplayedYourProjects = yourProjects.slice(0, PROJECTS_PER_PAGE);
      if (!isEmptyArray(yourProjects)) {
        yourProjects[0].isActive = true;
      }
      if (!isEmptyArray(sharedProjects)) {
        sharedProjects[0].isActive = false;
      }
    } else if (
      item === "sharedProjects" &&
      !sharedProjectsActive &&
      !isLoading // prevents toggling when loading
    ) {
      projectsActive = false;
      sharedProjectsActive = true;
      currentlyDisplayedSharedProjects = sharedProjects.slice(
        0,
        PROJECTS_PER_PAGE
      );
      if (!isEmptyArray(sharedProjects)) {
        sharedProjects[0].isActive = true;
      }
      if (!isEmptyArray(yourProjects)) {
        yourProjects[0].isActive = false;
      }
    }
  }

  function isEmptyArray(arr) {
    return arr.length === 0;
  }

  // when the page change function is triggered inside the pagination component, this function is called
  // which assigns a new slice of the all projects array using getCurrentPageItems defined in the pagination component
  // event.detail is equal to the return value of getCurrentPageItems
  function handlePaginationChange(event) {
    projectsActive
      ? (currentlyDisplayedYourProjects = event.detail)
      : (currentlyDisplayedSharedProjects = event.detail);
  }

  onMount(() => {
    loadAndDisplayProjects();
  });
</script>

<div class="projects-wrapper">
  <div class="projects-wrapper__top-section">
    <h1>{$_("projects.projects")}</h1>
    <div class="projects-wrapper__line"></div>
    <div class="projects-wrapper__nav-wrapper">
      <div
        class:active={projectsActive}
        on:click={() => toggleActive("projects")}
        on:keydown={() => toggleActive("projects")}
      >
        <h3>{$_("projects.yourProjects")}</h3>
      </div>
      <div
        class:active={sharedProjectsActive}
        on:click={() => toggleActive("sharedProjects")}
        on:keydown={() => toggleActive("sharedProjects")}
      >
        <h3>{$_("projects.sharedWithYou")}</h3>
      </div>
    </div>
    {#if isLoading}
      <div>{$_("projects.loadingProjects")}</div>
    {:else if errorLoadingProjects}
      <div>{$_("projects.errorLoadingProjects")}</div>
    {:else if projectsActive}
      <Pagination
        projectPerPage={PROJECTS_PER_PAGE}
        itemArray={yourProjects}
        on:pageChange={handlePaginationChange}
      />
    {:else if sharedProjectsActive}
      <Pagination
        projectPerPage={PROJECTS_PER_PAGE}
        itemArray={sharedProjects}
        on:pageChange={handlePaginationChange}
      />
    {/if}
  </div>
  <div class="projects-wrapper__bottom-section">
    {#if projectsActive && !isLoading && !errorLoadingProjects}
      <CreateProject on:updateProjectsPage={loadAndDisplayProjects} />
      {#each currentlyDisplayedYourProjects as project}
        <ProjectEntry
          projectName={project.name}
          isActive={project.isActive}
          toggleActive={() => {
            currentlyDisplayedYourProjects = toggleProjectActive(
              project.id,
              currentlyDisplayedYourProjects
            );
          }}
        />
      {/each}
    {:else if sharedProjectsActive}
      {#if isEmptyArray(sharedProjects)}
        <h3>{$_("projects.noProjectsFirstLine")}</h3>
        <h3>{$_("projects.noProjectsSecondLine")}</h3>
      {:else}
        {#each currentlyDisplayedSharedProjects as project}
          <ProjectEntry
            projectName={project.name}
            isActive={project.isActive}
            toggleActive={() => {
              currentlyDisplayedSharedProjects = toggleProjectActive(
                project.id,
                currentlyDisplayedSharedProjects
              );
            }}
          />
        {/each}
      {/if}
    {/if}
  </div>
</div>

<style lang="scss">
  .projects-wrapper {
    margin-top: 100px;
    width: 100%;
    height: 900px;

    &__bottom-section {
      width: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
    }

    &__top-section {
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    &__line {
      min-width: 200px;
      max-width: 500px;
      width: 100%;
      height: 2px;
      background-color: black;
    }

    & h1 {
      margin: 0 0 20px 0;
    }

    &__nav-wrapper {
      display: flex;
      text-align: center;
      margin-bottom: 40px;

      & div {
        padding: 10px 20px;

        &:hover {
          cursor: pointer;
          font-weight: bold;
        }
      }
      div.active {
        background-color: var(--whisper);
        font-weight: bold;
        cursor: default;
      }

      & h3 {
        margin: 0;
        font-weight: inherit;
      }
    }
  }
</style>
