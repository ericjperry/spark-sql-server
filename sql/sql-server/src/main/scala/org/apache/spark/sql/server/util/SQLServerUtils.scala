/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.server.util

import java.lang.reflect.Field

import org.apache.spark.SparkConf
import org.apache.spark.sql.internal.SQLConf
import org.apache.spark.util.Utils

object SQLServerUtils {

  def checkIfKerberosEnabled(conf: SparkConf): Boolean = {
    conf.contains("spark.yarn.keytab")
  }

  def checkIfKerberosEnabled(conf: SQLConf): Boolean = {
    conf.contains("spark.yarn.keytab")
  }

  // https://blog.sebastian-daschner.com/entries/changing_env_java
  def injectEnvVar(key: String, value: String): Unit = {
    val clazz = Utils.classForName("java.lang.ProcessEnvironment")
    injectIntoUnmodifiableMap(key, value, clazz)
  }

  private def getDeclaredField(clazz: Class[_], fieldName: String): Field = {
    val field = clazz.getDeclaredField(fieldName)
    field.setAccessible(true)
    field
  }

  private def injectIntoUnmodifiableMap(key: String, value: String, clazz: Class[_]): Unit = {
    val unmodifiableEnvField = getDeclaredField(clazz, "theUnmodifiableEnvironment")
    val unmodifiableEnv = unmodifiableEnvField.get(null)
    val unmodifiableMapClazz = Utils.classForName("java.util.Collections$UnmodifiableMap")
    val field = getDeclaredField(unmodifiableMapClazz, "m")
    field.get(unmodifiableEnv).asInstanceOf[java.util.Map[String, String]].put(key, value)
  }
}
