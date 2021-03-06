/*
 Copyright (C) 2015 Electronic Arts Inc.  All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1.  Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
 2.  Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
 3.  Neither the name of Electronic Arts, Inc. ("EA") nor the names of
     its contributors may be used to endorse or promote products derived
     from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY ELECTRONIC ARTS AND ITS CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL ELECTRONIC ARTS OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ea.orbit.actors.runtime;

import com.ea.orbit.actors.IAddressable;
import com.ea.orbit.actors.IRemindable;
import com.ea.orbit.actors.cluster.INodeAddress;
import com.ea.orbit.concurrent.Task;

import java.lang.reflect.Method;
import java.time.Clock;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Interface used by the generated code to interact with the orbit actors runtime.
 */
public interface IRuntime
{
    /**
     * Sends a message to an actor or observer.
     *
     * @param toReference destination actor reference or observer reference
     * @param oneWay      should expect an answer,
     *                    if false the task is completed with null.
     * @param methodId    the generated id for the method
     * @param params      the method parameters, must all be serializable.
     * @return a future with the return value, or a future with null (if one-way)
     */
    Task<?> sendMessage(IAddressable toReference, boolean oneWay, int methodId, Object[] params);

    /**
     * Handles calls to actor reference methods.
     *
     * @param toReference destination actor reference or observer reference
     * @param oneWay      should expect an answer,
     *                    if false the task is completed with null.
     * @param methodId    the generated id for the method
     * @param params      the method parameters, must all be serializable.
     * @return a future with the return value, or a future with null (if one-way)
     */
    Task<?> invoke(IAddressable toReference, Method m, boolean oneWay, final int methodId, final Object[] params);

    /**
     * Registers a timer to for the orbit actor
     *
     * @param actor        the actor requesting the timer.
     * @param taskCallable a callable that must return a task.
     * @param dueTime      the first time the timer will tick.
     * @param period       the period of subsequent ticks (if ZERO then will only tick once)
     * @param timeUnit     the time unit for period and dueTime
     * @return a registration that allows to cancel the timer.
     */
    Registration registerTimer(OrbitActor<?> actor, Callable<Task<?>> taskCallable, long dueTime, long period, TimeUnit timeUnit);

    /**
     * Gets the local clock. It's usually the system clock, but it can be changed for testing.
     *
     * @return the clock that should be used for checking the time during tests.
     */
    Clock clock();

    /**
     * Registers or updated a persisted reminder.
     *
     * @param actor        the reference to the actor.
     * @param reminderName the remainder's name
     * @param dueTime      how long since now the first tick should be triggered.
     * @param period       after the first tick, how often should the reminder be called.
     * @param timeUnit     the time unit for dueTime and period
     * @return completion promise for this operation
     */
    Task<?> registerReminder(IRemindable actor, String reminderName, long dueTime, long period, TimeUnit timeUnit);

    /**
     * Removes a previously registered reminder.
     *
     * @param actor        the actor that registered this reminder
     * @param reminderName the remainder's name
     * @return completion promise for this operation
     */
    Task<?> unregisterReminder(IRemindable actor, String reminderName);

    /**
     * Gets a string that represents uniquely the node that currently holds this actor.
     *
     * @return unique identity string
     */
    String runtimeIdentity();

    /**
     * Locates the node address of an actor.
     *
     * @param forceActivation a node will be chosen to activate the actor if
     *                        it's not currently active.
     *                        Actual activation is postponed until the actor receives one message.
     * @return actor address, null if actor is not active and forceActivation==false
     */
    Task<INodeAddress> locateActor(final IAddressable actorReference, final boolean forceActivation);

}
