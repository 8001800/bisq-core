/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.dao.node.consensus;

import bisq.core.dao.blockchain.ReadableBsqBlockChain;
import bisq.core.dao.blockchain.vo.Tx;
import bisq.core.dao.blockchain.vo.TxOutput;
import bisq.core.dao.blockchain.vo.TxOutputType;
import bisq.core.dao.blockchain.vo.TxType;

import bisq.common.app.Version;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Verifies if OP_RETURN data matches rules for a compensation request tx and applies state change.
 */
public class OpReturnCompReqController {
    private final ReadableBsqBlockChain readableBsqBlockChain;

    @Inject
    public OpReturnCompReqController(ReadableBsqBlockChain readableBsqBlockChain) {
        this.readableBsqBlockChain = readableBsqBlockChain;
    }

    public boolean verify(byte[] opReturnData, long bsqFee, int blockHeight, TxOutputsController.MutableState mutableState) {
        return mutableState.getCompRequestIssuanceOutputCandidate() != null &&
                opReturnData.length == 22 &&
                Version.COMPENSATION_REQUEST_VERSION == opReturnData[1] &&
                bsqFee == readableBsqBlockChain.getCreateCompensationRequestFee(blockHeight) &&
                readableBsqBlockChain.isCompensationRequestPeriodValid(blockHeight);
    }

    public void applyStateChange(Tx tx, TxOutput opReturnTxOutput, TxOutputsController.MutableState mutableState) {
        opReturnTxOutput.setTxOutputType(TxOutputType.COMPENSATION_REQUEST_OP_RETURN_OUTPUT);
        checkArgument(mutableState.getCompRequestIssuanceOutputCandidate() != null,
                "mutableState.getCompRequestIssuanceOutputCandidate() must not be null");
        mutableState.getCompRequestIssuanceOutputCandidate().setTxOutputType(TxOutputType.COMPENSATION_REQUEST_ISSUANCE_CANDIDATE_OUTPUT);
        tx.setTxType(TxType.COMPENSATION_REQUEST);
    }
}
