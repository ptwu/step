// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// true represents that the blinking text is hidden, false shows
let alternateOnOff = false;

// Function to be run on index page load. Calls all initial functions.
function init() {
  typeWriterEffect(0, 0);
  displayServletContent(5);
}

// Interval for repeated blinking text effect on page
setInterval(() => {
  if (alternateOnOff) {
    document.getElementById("blinking-text").style.opacity = 0;
  } else {
    document.getElementById("blinking-text").style.opacity = 1;
  }
  alternateOnOff = !alternateOnOff;
}, 500);

function scrollToProjects() {
  document.getElementById("projects").scrollIntoView({ behavior: "smooth" });
}

/**
 * Recursive function that fades out the tw-text div gradually over one second.
 * @param {number} currOpacity
 */
function fadeOutText(currOpacity) {
  setTimeout(() => {
    document.getElementById("tw-text").style.opacity = currOpacity;
    if (currOpacity > 0) {
      fadeOutText(currOpacity - 0.1);
    }
  }, 100);
}

/**
 * Typewriter effect loop that applies the effect to each string in the facts
 * array. Takes the current char index being written and the index of the current fact
 * in the facts array.
 * @param {number} charIndex @param {number} currentFactIndex
 */
function typeWriterEffect(charIndex, currentFactIndex) {
  const facts = [
    "I'm a rising sophomore at Cornell University.",
    "I like making things that make a marked improvement on our lives.",
    "I'm an avid badminton player!",
    "My Chinese name is Wu Lingrui (吴灵锐).",
  ];
  const currentFact = facts[currentFactIndex];

  if (charIndex < currentFact.length) {
    document.getElementById("tw-text").innerHTML += currentFact.charAt(
      charIndex
    );
    setTimeout(() => typeWriterEffect(charIndex + 1, currentFactIndex), 45);
  } else {
    fadeOutText(1);
    setTimeout(() => {
      document.getElementById("tw-text").innerHTML = "&nbsp;";
      setTimeout(() => {
        //reset fade out
        document.getElementById("tw-text").style.opacity = 1;
        typeWriterEffect(0, (currentFactIndex + 1) % facts.length);
      }, 55);
    }, 2500);
  }
}

/**
 * Sorts the comment array according to the current value of the select
 * dropdown menu
 * @param {Array} commentObjArray contains objects with username, text, 
 * and timestamp fields
 */
function sortCommentArr(commentObjArray) {
  const currentSortOption = document.getElementById("comment-sort-menu").value;
  switch (currentSortOption) {
    case "descending-time":
      commentObjArray.sort((a, b) => b.timestamp - a.timestamp);
      break;
    case "ascending-time":
      commentObjArray.sort((a, b) => a.timestamp - b.timestamp);
      break;
    case "descending-len":
      commentObjArray.sort((a, b) => b.text.length - a.text.length);
      break;
    case "ascending-len":
      commentObjArray.sort((a, b) => a.text.length - b.text.length);
      break;
    case "scramble":
      commentObjArray.sort(() => Math.random() - 0.5);
      break;
  }
}

/**
 * Renders the plaintext as a result of calling the /data GET endpoint by
 * setting the `comments-container` div to the text with certain number of
 * comments list. Throws an error if the endpoint does not return a JSON
 * array.
 * @param {number} numCommentsToShow. If === -1, then show all comments.
 */
async function displayServletContent(numCommentsToShow) {
  const res =
    numCommentsToShow === -1
      ? await fetch("/data")
      : await fetch(`/data?limit=${numCommentsToShow}`);
  const json = await res.json();
  if (!Array.isArray(json)) {
    throw new Error("Response data is not an array");
  }

  if (json.length === 0) {
    document.getElementById("comments-section").innerHTML =
      "<h5>No comments to show.</h5>";
    document.getElementById("comment-delete-button").style.display = "none";
  } else {
    sortCommentArr(json);
    document.getElementById("comments-section").innerHTML = json
      .map(
        ({ name, text, timestamp }) => `
            <div class="comments-card">
              <div class="comments-card-header">
                <img
                  src="/assets/images/account_circle-24px.svg"
                  alt="Icon of comment user"
                  class="comments-icon"
                />
                <span class="comments-card-user">
                  ${name} · ${new Date(timestamp).toLocaleDateString()}
                </span>
              </div>
              <p class="card-text">${text}</p>
            </div>
          `
      )
      .join("");
    document.getElementById("comment-delete-button").style.display = "block";
  }
}

/**
 * Displays servlet comments at a limit determined by the parameter string.
 * @param {string} value - the option of the select dropdown for comment limit numbers.
 */
function displayServletContentUsingString(value) {
  switch (value) {
    case "5":
      displayServletContent(5);
      break;
    case "10":
      displayServletContent(10);
      break;
    case "all":
      displayServletContent(-1);
      break;
    case "none":
      displayServletContent(0);
      break;
    default:
      throw new Error("Unimplemented # comments encountered");
  }
}

function rerenderCommentsWithCurrentLimit() {
  const limit = document.getElementById("comment-number-shown").value;
  displayServletContentUsingString(limit);
}

// Listen for changes in comment number selected and rerender comments section
// as needed. Throws an error if cases for 5, 10, all, or none are not
// encountered.
const selected = document.querySelector("#comment-number-shown");
selected.addEventListener("change", (event) => {
  displayServletContentUsingString(event.target.value);
});

// Listen for changes in sort method and rerender comments as needed.
const sortMenuElement = document.querySelector("#comment-sort-menu");
sortMenuElement.addEventListener("change", (event) => {
  rerenderCommentsWithCurrentLimit();
});

// Prevent comment form submit button from automatically refreshing upon click
const formElement = document.getElementById("comments-form");
formElement.addEventListener("submit", (event) => {
  event.preventDefault();
});

/**
 * POST request to the /data endpoint with query params for username and text
 * corresponding to the form fields; alerts if response code is not 2xx
 */
function addComment() {
  const username = document.getElementById("comment-username").value;
  const text = document.getElementById("comment-input").value;
  fetch(`/data?username=${username}&text=${text}`, { method: "POST" }).then(
    (res) => {
      if (res.ok) {
        rerenderCommentsWithCurrentLimit();
        document.getElementById("comment-username").value = "";
        document.getElementById("comment-input").value = "";
      } else {
        alert("Error: Enter a valid comment.");
      }
    }
  );
}

async function deleteComments() {
  await fetch("/delete-data", { method: "POST" });
  displayServletContent(0);
}
