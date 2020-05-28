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

/**
 * Adds a random fact about Peter to the page.
 */
function addRandomFact() {
  const facts = [
    "I was born in Trenton, NJ.",
    "I like bird watching!",
    "I play badminton a lot!",
    "My Chinese name is Wu Lingrui (吴灵锐).",
  ];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];
}

// true represents that the blinking text is hidden, false shows
let alternateOnOff = false;

// Interval for repeated blinking text effect on page
setInterval(() => {
  if (alternateOnOff) {
    document.getElementById("BlinkingText").style.opacity = 0;
  } else {
    document.getElementById("BlinkingText").style.opacity = 1;
  }
  alternateOnOff = !alternateOnOff;
}, 500);

function scrollToProjects() {
  document.getElementById("projects").scrollIntoView({ behavior: "smooth" });
}
