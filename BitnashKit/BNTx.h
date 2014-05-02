//
//  BNTx.h
//  BitnashKit
//
//  Created by Rich Collins on 3/24/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNObject.h"
#import "BNTxOut.h"

@class BNWallet;

@interface BNTx : BNObject

@property (weak) BNWallet *wallet;

@property NSMutableArray *inputs;
@property NSMutableArray *outputs;

@property NSString *txHash;

@property NSNumber *netValue;
@property NSNumber *updateTime;
@property NSString *counterParty;

- (BNTxOut *)newOutput;
//Creates a new BNTxOut and adds it to the outputs array.

- (void)configureForEscrowWithValue:(long long)value;
//sets up transaction for a 2 of 2 multisig output w/ given value.  Also selects inputs and change address.  Doesn't add fees.

- (void)subtractFee;
//subtracts the estimated fees from the first output.

- (void)sign;
//signs the inputs owned by wallet associated with this tx.

- (BNTx *)mergedWithEscrowTx:(BNTx *)tx;
//Returns a new BNTx that includes the inputs and outputs from this BNTx and the tx arg.  The multisig outputs are merged into a single multisig output with a value summed from the others and a set of pubkeys derrived from the first pubkey from any existing multisig output.

- (void)broadcast;
//Broadcast to the network.

- (BOOL)isConfirmed;
//YES if this tx is in a block from the longest chain.

- (void)markInputsAsSpent;
//Marks the inputs as spent so they won't be used for subsequent txs

- (void)markInputsAsUnspent;
//Marks the inputs as spent so they won't be used for subsequent txs

- (BNTx *)cancellationTx;
//Returns a new transaction that sends this transactions inputs back to this transactions wallet

@end
