//
//  BNTx.h
//  BitnashKit
//
//  Created by Rich Collins on 3/24/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNObject.h"

@class BNWallet;

@interface BNTx : BNObject

@property (weak) BNWallet *wallet;

@property NSMutableArray *inputs;
@property NSMutableArray *outputs;

@property NSString *hash;

- (id)sendToServer:(NSString *)message withArg:(id)arg;

- (id)sendToServer:(NSString *)message;

- (void)fillForValue:(long long)value;
//fills the Transaction with inputs, outputs and a public key based on the transactions value

- (void)subtractFee;
//subtracts the estimated fee from the first output.

- (void)sign;
//signs the inputs owned by wallet associated with this tx.

- (void)addInputsFromTx:(BNTx *)tx;
//Adds the inputs from tx.

- (void)mergeWithTx:(BNTx *)tx;
//Merges the inputs and outputs from tx.

- (void)broadcast;
//Broadcast to the network.

- (BOOL)isConfirmed;
//YES if this tx is in a block from the longest chain.

- (void)markInputsAsSpent;
//Marks the inputs as spent so they won't be used for subsequent txs

- (void)markInputsAsUnspent;
//Marks the inputs as spent so they won't be used for subsequent txs

- (void)ping;

@end
