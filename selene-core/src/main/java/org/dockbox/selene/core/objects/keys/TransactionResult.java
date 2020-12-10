/*
 *  Copyright (C) 2020 Guus Lieben
 *
 *  This framework is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 2.1 of the
 *  License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 *  the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.core.objects.keys;

public final class TransactionResult {

    public enum Status {
        FAILURE, SUCCESS
    }

    private static final TransactionResult SUCCESS = new TransactionResult(Status.SUCCESS, "");

    private final Status status;
    private final String message;

    private TransactionResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isSuccessfull() {
        return Status.SUCCESS == this.getStatus();
    }

    public static TransactionResult success() {
        return TransactionResult.SUCCESS;
    }

    public static TransactionResult fail(String message) {
        return new TransactionResult(Status.FAILURE, message);
    }

    public static TransactionResult fail(Throwable cause) {
        return new TransactionResult(Status.FAILURE, cause.getMessage());
    }

}
