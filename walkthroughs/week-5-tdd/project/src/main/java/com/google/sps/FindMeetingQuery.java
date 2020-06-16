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

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public final class FindMeetingQuery {

  /**
   * Computes a list of all time ranges where all attendees of a meeting are busy
   * @param  events    Collection of Event objects formed before the creation of this meeting
   * @param  attendees Collection of attendee string names
   * @return List of time ranges where attendees are engaged in other events.
   */
  private List<TimeRange> getBusyTimeRanges(Collection<Event> events, Collection<String> attendees) {
    List<TimeRange> busyTimeRanges = new ArrayList<TimeRange>();
    for (Event event : events) {
      Set<String> currEventAttendees = event.getAttendees();
      for (String attendee : attendees) {
        if (currEventAttendees.contains(attendee)) {
          busyTimeRanges.add(event.getWhen());
          break;
        }
      }
    }
    return busyTimeRanges;
  }

  /**
   * Removes nested and overlapping time ranges in a certain sorted List 
   * of TimeRange objects
   * @param  sortedtimeRanges  List of TimeRange objects that must be ordered by starting time
   */
  private void discretizeSortedTimeRanges(List<TimeRange> sortedTimeRanges) {
    ListIterator<TimeRange> iter = sortedTimeRanges.listIterator();
    TimeRange prevTimeRange = null;

    while (iter.hasNext()) {
      TimeRange curr = iter.next();
      if (prevTimeRange != null) {
        if (prevTimeRange.contains(curr)) {
          iter.remove();
        } else if (prevTimeRange.overlaps(curr)) {
          int end = Math.max(prevTimeRange.end(), curr.end());
          iter.set(TimeRange.fromStartEnd(prevTimeRange.start(), end, false));
        }
      }
      prevTimeRange = curr;
    }
  }

  /**
  * Adds a time range with certain start, end, duration, and inclusitivity attributes to a Collection of 
  * TimeRange objects if they fit the meeting requirements and are valid within a day.
  */
  private void addTimeRangeIfValid(Collection<TimeRange> availableTimeRanges, int start, int end, long duration,
      boolean inclusive) {
    if (start < end && end - start >= duration && end <= TimeRange.END_OF_DAY) {
      availableTimeRanges.add(TimeRange.fromStartEnd(start, end, inclusive));
    }
  }

  /**
   * Returns a Collection of TimeRange objects representing possible intervals of a certain meeting
   * occurring, with all participants able to go and having a certain possible duration.
   * @param  events  A Collection of Event objects formed before the creation of this meeting
   * @param  request A MeetingRequest object representing the meeting data.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> busyTimeRanges = getBusyTimeRanges(events, request.getAttendees());
    Collections.sort(busyTimeRanges, TimeRange.ORDER_BY_START);
    discretizeSortedTimeRanges(busyTimeRanges);

    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    if (busyTimeRanges.size() == 0) {
      return Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, true));
    }

    Collection<TimeRange> availableTimeRanges = new ArrayList<>();
    // Add from beginning of day to first busy time point
    addTimeRangeIfValid(availableTimeRanges, TimeRange.START_OF_DAY, busyTimeRanges.get(0).start(),
        request.getDuration(), false);

    for (int i = 0; i < busyTimeRanges.size() - 1; i++) {
      int currStart = busyTimeRanges.get(i).end();
      int currEnd = busyTimeRanges.get(i + 1).start();
      addTimeRangeIfValid(availableTimeRanges, currStart, currEnd, request.getDuration(), false);
    }

    addTimeRangeIfValid(availableTimeRanges, busyTimeRanges.get(busyTimeRanges.size() - 1).end(), TimeRange.END_OF_DAY,
        request.getDuration(), true);

    return availableTimeRanges;
  }
}
