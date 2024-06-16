<script>
  import { _ } from "svelte-i18n";
  import { connectedUsersStore } from "./scripts/store.js";
</script>

<div id="dashboard-container">
  <h4 class="dashboard-header">{$_("editor.editorDashboard.dashboard")}</h4>
  <div id="userList">
    <ul id="userArea">
      {#each Object.entries($connectedUsersStore) as [user, color] (user)}
        <li title={user} style="background-color: {color};">
          {user.charAt(0)}
        </li>
      {/each}
    </ul>
  </div>
</div>

<style>
  #dashboard-container {
    justify-content: flex-start;
    align-items: flex-start;
    flex-direction: column;
    display: flex;
    height: 27%;
    width: 100%;
    background-color: var(--white);
    border-left: none;
    text-align: left;
  }
  .dashboard-header {
    margin: 10px 0 0 10px;
    font-size: 18px;
  }

  #userList ul {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    list-style-type: none;
    padding: 0;
  }

  #userList ul li {
    padding: 10px;
    border: none;
    border-radius: 50%;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 5px;
    background-color: #2196f3;
    color: white;
    text-align: center;
    position: relative; /* Added for tooltip */
  }

  /* Tooltip styles */
  #userList ul li::after {
    content: attr(title);
    position: absolute;
    bottom: 100%;
    left: 50%;
    transform: translateX(-50%);
    background: #2f74bd;
    color: #fff;
    padding: 5px;
    border-radius: 5px;
    white-space: nowrap;
    opacity: 0;
    pointer-events: none; /* Prevents the tooltip from blocking hover on other elements */
    transition: opacity 0.2s ease-in-out;
  }

  #userList ul li:hover::after {
    opacity: 1; /* Shows the tooltip on hover */
  }
</style>
