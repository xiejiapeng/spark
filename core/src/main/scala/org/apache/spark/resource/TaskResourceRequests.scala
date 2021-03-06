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

package org.apache.spark.resource

import scala.collection.mutable

import org.apache.spark.resource.ResourceProfile._
import org.apache.spark.resource.ResourceUtils._

/**
 * A set of task resource requests. This is used in conjuntion with the ResourceProfile to
 * programmatically specify the resources needed for an RDD that will be applied at the
 * stage level.
 *
 * This api is currently private until the rest of the pieces are in place and then it
 * will become public.
 */
private[spark] class TaskResourceRequests() extends Serializable {

  private val _taskResources = new mutable.HashMap[String, TaskResourceRequest]()

  def requests: Map[String, TaskResourceRequest] = _taskResources.toMap

  /**
   * Specify number of cpus per Task.
   *
   * @param amount Number of cpus to allocate per Task.
   */
  def cpus(amount: Int): this.type = {
    val t = new TaskResourceRequest(CPUS, amount)
    _taskResources(CPUS) = t
    this
  }

  /**
   *  Amount of a particular custom resource(GPU, FPGA, etc) to use. The resource names supported
   *  correspond to the regular Spark configs with the prefix removed. For instance, resources
   *  like GPUs are resource.gpu (spark configs spark.task.resource.gpu.*)
   *
   * @param resourceName Name of the resource.
   * @param amount Amount requesting as a Double to support fractional resource requests.
   *               Valid values are less than or equal to 0.5 or whole numbers. This essentially
   *               lets you configure X number of tasks to run on a single resource,
   *               ie amount equals 0.5 translates into 2 tasks per resource address.
   */
  def resource(rName: String, amount: Double): this.type = {
    val t = new TaskResourceRequest(rName, amount)
    _taskResources(rName) = t
    this
  }

  def addRequest(treq: TaskResourceRequest): this.type = {
    _taskResources(treq.resourceName) = treq
    this
  }

  override def toString: String = {
    s"Task resource requests: ${_taskResources}"
  }
}
