//
//  BNWallet.h
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNServer.h"
#import "BNEscrowTx.h"

@interface BNWallet : NSObject

//1. Get Balance (seller)
//2. Get Deposit Address (seller)
//3. Get Balance (seller)
//4. Get Escrow Transaction (inputs / outputs/ pubkey) (seller)
//5. Serialize TX and send to buyer (seller)
//6. Get Escrow Transaction (inputs / outputs/ pubkey) (buyer)
//7. Load Serialized TX from buyer (buyer)
//8. Merge TXs (buyer)
//9. Sign TX (buyer)
//10. Serialize TX and send to seller (buyer)
//11. Load Serialized TX from buyer (seller)
//12. Broadcast TX  (seller)
//13. Get Conf Count (buyer/seller)
//14. ...
//15. Get Release TX (buyer)
//15. GOTO 5 for Release TX

@property BNServer *server;
//Start a BNServer and set it on this wallet prior to use.


- (NSError *) error;
//Error for last operation.

- (NSNumber *)balance;
//return the balance of the wallet in Satoshi.  Blocking call.

- (NSString *)createAddress;
//Create a new keypair for the wallet and return the address.

- (BNEscrowTx *)newEscrowTransaction;
//Create a new BNEscrowTransaction for this Wallet and return it.

- (void)debugWriteTxFile;
- (void)debugMergeWithThenSign:(BNWallet *)otherWallet;


@end
