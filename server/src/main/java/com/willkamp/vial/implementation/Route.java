package com.willkamp.vial.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Borrowed from @see <href= https://github.com/xjdr/xio>xio</href=> */
class Route {

  private static final Pattern keywordPattern = Pattern.compile("(:\\w+|:\\*\\w+)");
  private final String path;
  private final Pattern pathPattern;
  private final List<String> keywords;

  private Route(String path, Pattern pathPattern, List<String> keywords) {
    this.path = path;
    this.pathPattern = pathPattern;
    this.keywords = keywords;
  }

  private static Pattern compile(String pattern, List<String> keywords) {
    StringBuilder regexPattern = new StringBuilder();

    if (pattern.equals("/")) {
      regexPattern.append("/");
    } else {
      final String[] segments = pattern.split("/");

      for (String segment : segments) {
        if (!segment.equals("")) {
          regexPattern.append("/");
          if (keywordPattern.matcher(segment).matches()) {
            String keyword = segment.substring(1);

            if (keyword.indexOf("*") == 0) {
              keyword = keyword.substring(1);
              regexPattern.append("(?<").append(keyword).append(">.*)");
            } else {
              regexPattern.append("(?<").append(keyword).append(">[^/]*)");
            }
            keywords.add(keyword);
          } else {
            regexPattern.append(segment);
          }
        }
      }
    }
    regexPattern.append("[/]?");

    return Pattern.compile(regexPattern.toString());
  }

  static Route build(String pattern) {
    List<String> keywords = new ArrayList<>();
    return new Route(pattern, compile(pattern, keywords), keywords);
  }

  Pattern pathPattern() {
    return pathPattern;
  }

  boolean matches(CharSequence path) {
    return pathPattern.matcher(path).matches();
  }

  Map<String, String> groups(CharSequence path) {
    Matcher matcher = pathPattern.matcher(path);
    Map<String, String> groups = new HashMap<>();
    if (matcher.matches()) {
      for (String keyword : keywords) {
        groups.put(keyword, matcher.group(keyword));
      }
    }
    return groups;
  }

  // Let's just assume that if two Route objects have been built
  // from the same path that they will have the same pattern and
  // keywords.

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Route)) return false;
    final Route other = (Route) o;
    return other.path.equals(path);
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }
}
