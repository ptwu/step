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
      : await fetch(`/data?list=${numCommentsToShow}`);
  const json = await res.json();
  if (!Array.isArray(json)) {
    throw new Error("Response data is not an array");
  }

  document.getElementById("comments-section").innerHTML =
    json.length === 0
      ? "<h5>No comments to show.</h5>"
      : '<ul class="comments-list">' +
        json
          .map(({ name, text, _ }) => `<li><b>${name}: </b>${text}</li>`)
          .join("") +
        "</ul>";
}

// Listen for changes in comment number selected and rerender comments section
// as needed. Throws an error if cases for 5, 10, all, or none are not
// encountered.
const selected = document.querySelector("#comment-number-shown");
selected.addEventListener("change", (event) => {
  switch (event.target.value) {
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
});
