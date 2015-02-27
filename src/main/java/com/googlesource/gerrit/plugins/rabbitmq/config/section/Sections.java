// Copyright (C) 2015 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.rabbitmq.config.section;

import com.googlesource.gerrit.plugins.rabbitmq.annotation.Default;
import com.googlesource.gerrit.plugins.rabbitmq.annotation.Limit;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;

public final class Sections {
  private static final Logger LOGGER = LoggerFactory.getLogger(Sections.class);

  public static final <T extends Section> String getName(T section) {
    return section.getClass().getSimpleName().toLowerCase();
  }

  public static final <T extends Section> T initialize(T section) {
    Field[] fs = section.getClass().getFields();
    for (Field f : fs) {
      try {
        if (f.isAnnotationPresent(Default.class)) {
          Default a = f.getAnnotation(Default.class);
          Class<?> type = f.getType();
          if (type == String.class) {
            f.set(section, new String(a.value()));
          } else if (type == Integer.class) {
            f.set(section, new Integer(a.value()));
          } else if (type == Long.class) {
            f.set(section, new Long(a.value()));
          } else if (type == Boolean.class) {
            f.set(section, new Boolean(a.value()));
          }
        }
      } catch (Exception ex) {
        LOGGER.warn("Exception during initialize: {}", f.getName());
      }
    }
    return section;
  }

  public static final <T extends Section> Config toConfig(T section) {
    return toConfig(section, new Config());
  }

  public static final <T extends Section> Config toConfig(T section, Config config) {
    Field[] fs = section.getClass().getFields();
    for (Field f : fs) {
      try {
        Class<?> type = f.getType();
        Object obj = f.get(section);
        if (obj != null) {
          if (type == String.class) {
            config.setString(getName(section), null, f.getName(), String.class.cast(obj));
          } else if (type == Integer.class) {
            config.setInt(getName(section), null, f.getName(), Integer.class.cast(obj));
          } else if (type == Long.class) {
            config.setLong(getName(section), null, f.getName(), Long.class.cast(obj));
          } else if (type == Boolean.class) {
            config.setBoolean(getName(section), null, f.getName(), Boolean.class.cast(obj));
          }
        }
      } catch (Exception ex) {
        LOGGER.warn("Exception during toConfig: {}", f.getName());
        LOGGER.info("{}", ex.getMessage());
      }
    }
    return config;
  }

  public static final <T extends Section> Section fromConfig(T section, Config... configs) {
    for (Config config : configs) {
      if (config != null) {
        Set<String> names = config.getNames(getName(section));
        Field[] fs = section.getClass().getFields();

        for (Field f : fs) {
          try {
            if (names.contains(f.getName())) {
              Class<?> type = f.getType();
              if (type == String.class) {
                f.set(section, new String(config.getString(getName(section), null, f.getName())));
              } else if (type == Integer.class) {
                f.set(section, new Integer(config.getInt(getName(section), null, f.getName(), 0)));
              } else if (type == Long.class) {
                f.set(section, new Long(config.getLong(getName(section), null, f.getName(), 0)));
              } else if (type == Boolean.class) {
                f.set(section, new Boolean(config.getBoolean(getName(section), null, f.getName(), false)));
              }
            }
          } catch (Exception ex) {
            LOGGER.warn("Exception during fromConfig: {}", f.getName());
          }
        }
      }
    }
    return section;
  }

  public static final <T extends Section> T normalize(T section) {
    Field[] fs = section.getClass().getFields();
    for (Field f : fs) {
      try {
        if (f.getType() == Integer.class && f.isAnnotationPresent(Limit.class)) {
          Object obj = f.get(section);
          if (obj != null) {
            Integer val = Integer.class.cast(obj);
            Limit a = f.getAnnotation(Limit.class);
            if (a.min() != -1 && val < a.min()) {
              val = a.min();
            }
            if (a.max() != -1 && val > a.max()) {
              val = a.max();
            }
            f.set(section, val);
          }
        }
      } catch (Exception ex) {
        LOGGER.warn("Exception during normalize: {}", f.getName());
        LOGGER.info("{}", ex.getMessage());
      }
    }
    return section;
  }
}
