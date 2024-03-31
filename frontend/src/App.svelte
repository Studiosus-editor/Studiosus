<script>
  import { Route, Router } from "svelte-routing";
  import CodeEditor from "./Components/Editor/CodeEditor.svelte";
  import Footer from "./Components/Footer.svelte";
  import LayoutManager from "./Components/LayoutManager.svelte";
  import Login from "./Components/Login.svelte";
  import NotFound from "./Components/NotFound.svelte";
  import Profile from "./Components/Profile.svelte";
  import { isLoading } from "svelte-i18n";
  import Register from "./Components/Register.svelte";
  import TopNavigation from "./Components/TopNavigation.svelte";

</script>
{#if $isLoading}
  <!-- <p>Loading language settings...</p> -->
{:else}
  <LayoutManager header={TopNavigation} footer={Footer}>
    <Router>
      <Route path="/" let:params component={CodeEditor} />
      <Route path="/login" let:params component={Login} />
      <Route path="/register" let:params component={Register} />
      <Route path="/profile" let:params component={Profile} />
      <Route path="*" let:params component={NotFound} />
    </Router>
  </LayoutManager>
{/if}
