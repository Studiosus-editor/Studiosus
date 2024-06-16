<script>
  import { isLoading } from "svelte-i18n";
  import { Route, Router } from "svelte-routing";
  import Toasts from "./Components/Modal/ToastNotification/Toasts.svelte";
  import CodeEditor from "./Components/Editor/CodeEditor.svelte";
  import Footer from "./Components/Footer/Footer.svelte";
  import LayoutManager from "./Components/LayoutManager.svelte";
  import Login from "./Components/Login.svelte";
  import NotFound from "./Components/NotFound.svelte";
  import Profile from "./Components/Profile/Profile.svelte";
  import Projects from "./Components/Projects/Projects.svelte";
  import Register from "./Components/Register.svelte";
  import TopNavigation from "./Components/TopNavigation.svelte";
  import Templates from "./Components/Templates/Templates.svelte";
  import ForgotPassword from "./Components/ForgotPassword.svelte";
  import ResetPassword from "./Components/ResetPassword.svelte";
  import { onMount } from "svelte";

  function isProtectedRoute(path) {
    const protectedRoutes = ["/profile", "/projects"];
    if (protectedRoutes.includes(path)) {
      return true;
    }
    if (path.startsWith("/project/")) {
      const parts = path.split("/");
      return parts.length === 4;
    }
    return false;
  }

  onMount(() => {
    const currentPath = window.location.pathname;
    if (isProtectedRoute(currentPath) && !hasSessionCookie()) {
      window.location.href = "/login";
    }
  });

  function hasSessionCookie() {
    return document.cookie.includes("JSESSIONID=");
  }
</script>

{#if $isLoading}
  <!-- <p>Loading language settings...</p> -->
{:else}
  <LayoutManager
    toastNotifications={Toasts}
    header={TopNavigation}
    footer={Footer}
  >
    <Router>
      <Route path="/" let:params component={CodeEditor} />
      <Route path="/login" let:params component={Login} />
      <Route path="/register" let:params component={Register} />
      <Route path="/profile" let:params component={Profile} />
      <Route path="/projects" let:params component={Projects} />
      <Route path="/editor" let:params component={CodeEditor} />
      <Route path="/project/:id/:role" let:params>
        <CodeEditor {params} />
      </Route>
      <Route path="/templates" let:params component={Templates} />
      <Route path="/template/:id" let:params>
        <CodeEditor {params} isTemplate={true} />
      </Route>
      <Route path="/forgot-password" let:params component={ForgotPassword} />
      <Route path="/reset-password/:resetToken" let:params>
        <ResetPassword {params} />
      </Route>
      <Route path="*" let:params component={NotFound} />
    </Router>
  </LayoutManager>
{/if}
