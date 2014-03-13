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

@property NSNumber *value;
//Value to be locked in escrow via multisig output.  Denominated in satoshi.

@property NSMutableArray *inputs;
@property NSMutableArray *outputs;

- (void)fillForValue:(long long)value;
//fills the Transaction with inputs, outputs and a public key based on the transactions value

- (void)addFeeAndSign;
//signs the inputs owned by wallet associated with this tx.

- (NSNumber *)fee;
//Fee associated with this tx.  Denominated in satoshi.

- (void)mergeWithEscrowTx:(BNEscrowTx *)tx;
//Merges the outputs from tx.  The multisig output is merged by adding its value and pubKey to the multisig output of this tx

@end
