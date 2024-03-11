<script>
  import { Route, Router } from "svelte-routing";
  import CodeEditor from "./Components/Editor/CodeEditor.svelte";
  import Footer from "./Components/Footer.svelte";
  import Login from "./Components/Login.svelte";
  import TopNavigation from "./Components/TopNavigation.svelte";
  import NotFound from "./Components/NotFound.svelte";
  import Profile from "./Components/Profile.svelte";
</script>

<TopNavigation />
<main>
  <Router>
    <Route path="/" let:params component={CodeEditor} />
    <Route path="/login" let:params component={Login} />
    <Route path="/profile" let:params component={Profile} />
    <Route path="*" let:params component={NotFound} />
  </Router>
</main>
<Footer />

<style>
  main {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
  }
</style>
