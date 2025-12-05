package com.speedreader.trainer.data.repository;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DocumentRepository_Factory implements Factory<DocumentRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FirebaseAuth> firebaseAuthProvider;

  public DocumentRepository_Factory(Provider<Context> contextProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseAuth> firebaseAuthProvider) {
    this.contextProvider = contextProvider;
    this.firestoreProvider = firestoreProvider;
    this.firebaseAuthProvider = firebaseAuthProvider;
  }

  @Override
  public DocumentRepository get() {
    return newInstance(contextProvider.get(), firestoreProvider.get(), firebaseAuthProvider.get());
  }

  public static DocumentRepository_Factory create(Provider<Context> contextProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseAuth> firebaseAuthProvider) {
    return new DocumentRepository_Factory(contextProvider, firestoreProvider, firebaseAuthProvider);
  }

  public static DocumentRepository newInstance(Context context, FirebaseFirestore firestore,
      FirebaseAuth firebaseAuth) {
    return new DocumentRepository(context, firestore, firebaseAuth);
  }
}
