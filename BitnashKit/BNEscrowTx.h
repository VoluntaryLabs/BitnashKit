//
//  BNEscrowTransaction.h
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>

@class BNWallet;

@interface BNEscrowTx : NSObject

@property (weak) BNWallet *wallet;

@property NSMutableArray *inputs;
@property NSMutableArray *outputs;

@property NSString *hash;

- (void)fillForValue:(long long)value;
//fills the Transaction with inputs, outputs and a public key based on the transactions value

- (void)addFee;
//subtracts the estimated fee from the first output

- (void)sign;
//signs the inputs owned by wallet associated with this tx.

- (void)addInputsFromEscrowTx:(BNEscrowTx *)tx;
//Adds the inputs from tx.

- (void)mergeWithEscrowTx:(BNEscrowTx *)tx;
//Merges the inputs and outputs from tx.  The multisig output is merged by adding its value and pubKey to the multisig output of this tx

- (void)broadcast;
//Broadcast to the network.

@end
