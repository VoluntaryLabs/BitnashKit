//
//  BNTx.h
//  BitnashKit
//
//  Created by Rich Collins on 3/24/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNObject.h"
#import "BNTxOut.h"
#import "BNTxIn.h"

@class BNWallet;

@interface BNTx : BNObject

@property (weak) BNWallet *wallet;

@property NSMutableArray *inputs;
@property NSMutableArray *outputs;

@property NSString *txHash;
@property NSString *serializedHex;

@property NSNumber *netValue;
@property NSNumber *fee;
@property NSNumber *updateTime;
@property NSString *counterParty;
@property NSNumber *confirmations;
@property BNTx *subsumingTx;
@property (nonatomic) NSNumber *isSentToSelf;

@property (nonatomic) NSString *description;
@property (nonatomic) NSString *txType;

- (BNTxOut *)newOutput;
//Creates a new BNTxOut and adds it to the outputs array.

- (void)configureForOutputWithValue:(NSNumber *)value;
//sets up transaction to have a pay to address output w/ given value.  Also selects inputs and change address.  Doesn't add fees.

- (void)configureForEscrowWithValue:(NSNumber *)value;
//sets up transaction to have a 2of2 multisig output w/ given value.  Also selects inputs and change address.  Doesn't add fees.

- (void)configureForEscrowWithInputTx:(BNTx *)inputTx;
//completely spends the first output from inputTx (no change)

- (void)configureForReleaseWithInputTx:(BNTx *)inputTx;
//completely spends the first output from inputTx;

- (void)addPayToAddressOutputWithValue:(NSNumber *)value;

- (void)payToAddress:(NSString *)address value:(NSNumber *)value;

- (void)addInputsAndChange;

- (void)emptyWallet; //Spends all utxo

- (void)subtractFee;
//subtracts the estimated fees from the first output.

- (void)sign;
//signs the inputs owned by wallet associated with this tx.

- (void)signInput:(BNTxIn *)txIn;

- (void)lockInput:(BNTxIn *)txIn;
- (void)lockOutput:(BNTxOut *)txOut;

- (BNTx *)mergedWithEscrowTx:(BNTx *)tx;
//Returns a new BNTx that includes the inputs and outputs from this BNTx and the tx arg.  The multisig outputs are merged into a single multisig output with a value summed from the others and a set of pubkeys derrived from the first pubkey from any existing multisig output.

- (void)broadcast;
//Broadcast to the network.

- (BOOL)isConfirmed;
//YES if this tx is in a block from the longest chain.

- (BOOL)wasBroadcast;
//YES if this tx was broadcast

- (BOOL)isCancelled;

- (void)lockInputs;
//Locks the connected outputs so they won't be used for subsequent txs

- (void)unlockInputs;
//Unlocks the connected outputs so they can be used for subsequent txs

- (void)unlockOutputs;
//Unlocks the outputs so they can be used for subsequent txs

- (BNTx *)cancellationTx;
//Returns a new transaction that sends this transactions inputs back to this transactions wallet

- (NSString *)webUrl;

- (NSNumber *)changeValue;

- (BNTxOut *)firstOutput;

//YES if all inputs and outputs are the same (but not necessarily the txhash)
- (BOOL)isEquivalentTo:(BNTx *)otherTx;

//Returns self or a subsuming tx that is equivalent to this tx
- (BNTx *)canonicalTx;

//updateTime as an NSDate
- (NSDate *)updateTimeDate;

//for display in wallet
- (NSString *)updateTimeDescription;

//Used for sorting
- (NSNumber *)ordinality;

@end
