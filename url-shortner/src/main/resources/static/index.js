function handleSubmit(e) {
  const urlInput = document.getElementById("url-input").value;
  fetch("/url/sort", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ url: urlInput }),
  })
    .then((res) => {
      if (!res.ok) {
        throw new Error("Request failed with status: " + res.status);
      }
      return res.json();
    })
    .then(({ sortCode }) => {
      const newBadge = document.getElementById("new-url-badge");
      if (newBadge) {
        newBadge.remove();
      }

      const sortUrlListContainer = document.getElementById("sort-url-list");
      const urlEntry = document.createElement("a");
      urlEntry.className = "list-group-item";
      urlEntry.setAttribute("href", `${location.origin}/${sortCode}`);
      urlEntry.setAttribute("target", "_blank");
      urlEntry.innerHTML = `${sortCode} <span style="float:right;" class="badge bg-danger" id="new-url-badge">new</span>`;
      sortUrlListContainer.prepend(urlEntry);
    })
    .catch(alert);
}

(function () {
  fetch("/urls", {
    headers: {},
  })
    .then((res) => {
      if (!res.ok) {
        throw new Error("Request failed with status: " + res.status);
      }
      return res.json();
    })
    .then((urls) => {
      const sortUrlListContainer = document.getElementById("sort-url-list");
      urls.forEach((url) => {
        const urlEntry = document.createElement("a");
        urlEntry.className = "list-group-item";
        urlEntry.setAttribute("href", `${location.origin}/${url.sortCode}`);
        urlEntry.setAttribute("target", "_blank");
        urlEntry.innerText = url.sortCode;
        sortUrlListContainer.append(urlEntry);
      });
    })
    .catch(alert);
})();
