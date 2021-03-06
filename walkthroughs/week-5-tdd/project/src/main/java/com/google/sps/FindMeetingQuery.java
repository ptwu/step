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
   * Returns a Collection of TimeRange objects representing possible intervals of
   * a certain meeting occurring, with all participants able to go. If the 
   * request parameter has optional attendees, they will be accounted for
   * unless they block the meeting from occurring entirely.
   * 
   * @param events  A Collection of Event objects formed before the creation of
   *                this meeting
   * @param request A MeetingRequest object representing the meeting data.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> requiredTimeRanges = getBusyTimeRanges(events, request.getAttendees());
    List<TimeRange> optionalTimeRanges = getBusyTimeRanges(events, request.getOptionalAttendees());
    List<TimeRange> optionalAndRequiredTimeRanges = new ArrayList<>(
        requiredTimeRanges.size() + optionalTimeRanges.size());
    optionalAndRequiredTimeRanges.addAll(requiredTimeRanges);
    optionalAndRequiredTimeRanges.addAll(optionalTimeRanges);

    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    Collection<TimeRange> validMeetingTimesWithOptional = getValidMeetingTimeRanges(optionalAndRequiredTimeRanges,
        request);

    if (validMeetingTimesWithOptional.size() > 0 || requiredTimeRanges.size() == 0) {
      return validMeetingTimesWithOptional;
    }

    return getValidMeetingTimeRanges(requiredTimeRanges, request);
  }

  /**
   * Computes the valid meeting time ranges according to a certain MeetingRequest
   * and list of busy time ranges where the attendees cannot attend.
   * 
   * @param busyTimeRanges list of TimeRange objects representing the busy time
   *                       ranges, sorted, for a certain group.
   * @param request        MeetingRequest object representing the meeting
   *                       requirements
   * @return Collection of TimeRange objects representing valid meeting time
   *         intervals
   */
  private static Collection<TimeRange> getValidMeetingTimeRanges(List<TimeRange> busyTimeRanges, MeetingRequest request) {
    if (busyTimeRanges.size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    
    Collections.sort(busyTimeRanges, TimeRange.ORDER_BY_START);
    discretizeSortedTimeRanges(busyTimeRanges);

    Collection<TimeRange> availableTimeRanges = new ArrayList<>();
    // Add from beginning of day to first busy time point
    TimeRange BODToFirstBusyRange = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, busyTimeRanges.get(0).start(),
        false);
    addTimeRangeIfValid(availableTimeRanges, BODToFirstBusyRange, request.getDuration());

    for (int i = 0; i < busyTimeRanges.size() - 1; i++) {
      int currStart = busyTimeRanges.get(i).end();
      int currEnd = busyTimeRanges.get(i + 1).start();
      TimeRange currRange = TimeRange.fromStartEnd(currStart, currEnd, false);
      addTimeRangeIfValid(availableTimeRanges, currRange, request.getDuration());
    }

    TimeRange lastBusyToEODRange = TimeRange.fromStartEnd(busyTimeRanges.get(busyTimeRanges.size() - 1).end(),
        TimeRange.END_OF_DAY, true);
    addTimeRangeIfValid(availableTimeRanges, lastBusyToEODRange, request.getDuration());

    return availableTimeRanges;
  }

  /**
   * Computes a sorted list (by start time) of all time ranges where all attendees
   * of a meeting are busy
   * 
   * @param events    Collection of Event objects formed before the creation of
   *                  this meeting
   * @param attendees Collection of attendee string names
   * @return List of time ranges where attendees are engaged in other events.
   */
  private static List<TimeRange> getBusyTimeRanges(Collection<Event> events, Collection<String> attendees) {
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
   * Removes nested and overlapping time ranges in a certain sorted List of
   * TimeRange objects
   * 
   * @param sortedtimeRanges List of TimeRange objects that must be ordered by
   *                         starting time
   */
  private static void discretizeSortedTimeRanges(List<TimeRange> sortedTimeRanges) {
    ListIterator<TimeRange> iter = sortedTimeRanges.listIterator();
    TimeRange prevTimeRange = null;

    while (iter.hasNext()) {
      TimeRange curr = iter.next();
      if (prevTimeRange == null) {
        prevTimeRange = curr;
      } else {
        if (prevTimeRange.contains(curr)) {
          iter.remove();
        } else if (prevTimeRange.overlaps(curr)) {
          int end = Math.max(prevTimeRange.end(), curr.end());
          iter.set(TimeRange.fromStartEnd(prevTimeRange.start(), end, false));
        } else {
          prevTimeRange = curr;
        }
      }
    }
  }

  /**
   * Adds a time range with certain start, end, duration, and inclusitivity
   * attributes to a Collection of TimeRange objects if they fit the meeting
   * requirements and are valid within a day.
   */
  private static void addTimeRangeIfValid(Collection<TimeRange> availableTimeRanges, TimeRange range, long duration) {
    if (range.start() < range.end() && range.end() - range.start() >= duration) {
      availableTimeRanges.add(range);
    }
  }
}